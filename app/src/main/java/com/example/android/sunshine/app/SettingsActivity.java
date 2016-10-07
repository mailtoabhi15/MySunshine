package com.example.android.sunshine.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        // TODO: Add preferences from XML
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        // TODO: Add preferences
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_temp_units_key)));

    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
        else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }

        return true;
    }

    //Dixit-Imp: Lesson-5.41:There's an issue where, in 2 Pane mode, if we got to SettingsActivity & Click
    //Navigation UP Button, then the DetailFragment is Blank coz the BrandNew Instance of MainActivity
    //gets Launched & for this New Instance No Item is Selected yet.
    //To Fix this we need to get/navigate to the Previous Instance of MainActivity which has the Item selected
    //and not create the New Instance
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        //Dixit-imp:Fix to achieve what is mentioned above->we overide this method(getparentActivity), which
        // system will call for getting the parnetActivity intent when the Navigation UP Button is clicked.
        //Get the ParnetActivityIntent form the Super class, this will create a New Intent for mainActivity
        // then add flag , to check if the MainActivity is already Running in our Task and use that one insteadd
        // of creating its New Instance. This Api is added form JellyBean onwards
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}