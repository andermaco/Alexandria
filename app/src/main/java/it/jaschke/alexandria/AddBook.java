package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private final String EAN_CONTENT = "eanContent";
    private OnConnectionProvider mCallback;
    private EditText ean;
    private View rootView;

    public AddBook() {
    }

    public static boolean isNetworkAvailable(Context context, int[] networkTypes) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType : networkTypes) {
                NetworkInfo netInfo = cm.getNetworkInfo(networkType);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * http://stackoverflow.com/questions/17108621/converting-isbn10-to-isbn13
     *
     * @param ISBN10
     * @return
     */
    public static String ISBN10toISBN13(String ISBN10) {
        String ISBN13 = ISBN10;
        ISBN13 = "978" + ISBN13.substring(0, 9);
        //if (LOG_D) Log.d(TAG, "ISBN13 without sum" + ISBN13);
        int d;

        int sum = 0;
        for (int i = 0; i < ISBN13.length(); i++) {
            d = ((i % 2 == 0) ? 1 : 3);
            sum += ((((int) ISBN13.charAt(i)) - 48) * d);
            //if (LOG_D) Log.d(TAG, "adding " + ISBN13.charAt(i) + "x" + d + "=" + ((((int) ISBN13.charAt(i)) - 48) * d));
        }
        sum = 10 - (sum % 10);
        ISBN13 += sum;

        return ISBN13;
    }

    @Override
    public final void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ean != null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

// TODO: 12/11/15 Checks connectivity
        // Checks connectivity
        int networkTypes[] = {ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI};
        if (!isNetworkAvailable(getActivity(), networkTypes)) {
            mCallback.onNotConnectionProvided();
        }


        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);

        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
//@// TODO: 11/11/15  Added functionality to convert ISBN10 to ISBN13
                    ean = ISBN10toISBN13(ean);
                }
                if (ean.length() < 13) {
                    clearFields();
                    return;
                }
                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
                AddBook.this.restartLoader();
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    IntentIntegrator.forSupportFragment(AddBook.this).setPrompt("Alexandria")
                            .setBeepEnabled(true)
                            .initiateScan();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ean.setText("");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        if (savedInstanceState != null) {
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return rootView;
    }

    // TODO: 13/11/15 Added Zxing functionality
    @Override
    public final void onActivityResult(int requestCode, int resultCode, Intent data) {
        // retrieve scan result
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(
                requestCode, resultCode, data);
        if (scanningResult != null) {
            // we have a result
            String code = scanningResult.getContents();
            ean.setText(code);
        } else {
            Toast.makeText(getActivity(),
                    "No scan data received!", Toast.LENGTH_SHORT).show();
        }

    }

    private void restartLoader() {
        int LOADER_ID = 1;
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public final android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (ean.getText().length() == 0) {
            return null;
        }
        String eanStr = ean.getText().toString();
        if (eanStr.length() == 10 && !eanStr.startsWith("978")) {
            eanStr = ISBN10toISBN13(eanStr);
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public final void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors != null && authors.length() > 0) {
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        }
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (imgUrl != null && imgUrl.length() > 0) {

            if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
//                new DownloadImage(getActivity(), (ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
// TODO: 18/11/15 Added Piccaso lib
                Picasso.with(getContext())
                        .load(imgUrl)
                        .into((ImageView) rootView.findViewById(R.id.bookCover));

                rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
            }
        }
        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields() {
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public final void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);

        try {
            mCallback = (OnConnectionProvider) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }

    }

    // Container Activity must implement this interface
    public interface OnConnectionProvider {
        void onNotConnectionProvided();
    }
}
