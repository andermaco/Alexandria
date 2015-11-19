package it.jaschke.alexandria;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * Created by andermaco on 16/11/15.
 */
public class PrefFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {
    public static final String KEY_PREF_SYNC_CONN = "list_preference";

    @Override
    public final void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.fragment_preference);
        getActionBar().setTitle(getString(R.string.action_settings));
    }

    @Override
    public final void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_SYNC_CONN)) {
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }
}
