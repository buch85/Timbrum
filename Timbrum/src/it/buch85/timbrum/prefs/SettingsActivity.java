package it.buch85.timbrum.prefs;

import it.buch85.timbrum.R;
import it.buch85.timbrum.R.xml;
import android.os.Bundle;
import android.preference.PreferenceActivity;


public class SettingsActivity extends PreferenceActivity  {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
