package it.jaschke.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.data.AlexandriaContract.AuthorEntry;
import it.jaschke.alexandria.data.AlexandriaContract.BookEntry;
import it.jaschke.alexandria.data.AlexandriaContract.CategoryEntry;
import it.jaschke.alexandria.services.BookService;


public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EAN_KEY = "EAN";
    private View rootView;
    private String ean;
    private ShareActionProvider shareActionProvider;

    public BookDetail() {
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public final View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            ean = arguments.getString(BookDetail.EAN_KEY);
            int LOADER_ID = 10;
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
            }
        });
        return rootView;
    }


    @Override
    public final void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }



    @Override
    public final android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public final void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.fullBookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(BookEntry.DESC));
        ((TextView) rootView.findViewById(R.id.fullBookDesc)).setText(desc);

        String authors = data.getString(data.getColumnIndex(AuthorEntry.AUTHOR));
        if (authors != null && authors.length() > 0) {
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        }
        String imgUrl = data.getString(data.getColumnIndex(BookEntry.IMAGE_URL));
        if (imgUrl != null && imgUrl.length() > 0) {
            if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
                Picasso.with(getContext())
                        .load(imgUrl)
                        .into((ImageView) rootView.findViewById(R.id.fullBookCover));
                rootView.findViewById(R.id.fullBookCover).setVisibility(View.VISIBLE);
            }
        }
        String categories = data.getString(data.getColumnIndex(CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        if (rootView.findViewById(R.id.right_container) != null) {
            rootView.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
        }

        // Checks null in case e.g. activity gets destroyed
        if (shareActionProvider != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            }
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + bookTitle);
            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
    }
}