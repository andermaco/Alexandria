package it.jaschke.alexandria;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PreferencesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PreferencesFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreferencesFragment extends Fragment {
    public static final String TAG = PreferencesFragment.class.getSimpleName();
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private OnFragmentInteractionListener mListener;
    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;

    public PreferencesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preferences, container, false);
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (checkedId == R.id.radioButton_books) {
                    sp.edit().putInt(STATE_SELECTED_POSITION, 0).apply();
                    sp.edit().putString("pref_startFragment", "0").apply();
                    Toast.makeText(getActivity(), "saved books", Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.radioButton_scan) {
                    sp.edit().putInt(STATE_SELECTED_POSITION, 1).apply();
                    sp.edit().putString("pref_startFragment", "1").apply();
                    Toast.makeText(getActivity(), "saved scan", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (mCurrentSelectedPosition == 0) {
            RadioButton radioButton = (RadioButton) view.findViewById(R.id.radioButton_books);
            radioButton.setChecked(true);
        } else {
            RadioButton radioButton = (RadioButton) view.findViewById(R.id.radioButton_scan);
            radioButton.setChecked(true);
        }
        return view;
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.action_settings);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle(R.string.action_settings);
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction();
    }

}
