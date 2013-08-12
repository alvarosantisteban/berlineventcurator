package com.alvarosantisteban.berlincurator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Creates the settings 
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	 //public static final String KEY_PREF_SYNC_CONN = "multilist_sites";
	 public static final String PREFS_FILE_NAME = "multilist_sites";
	 public static final String KEY_PREF_MULTILIST_SITES = "multilist_sites";
	 Set<String> selectedWebsites;
	
	 /**
	  * Loads the preferences from the XML file
	  */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("ON CREATE THE SETTINGS FRAGMENT");
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Show the list to the user with the selection stored in the preferences
        MultiSelectListPreference connectionPref = (MultiSelectListPreference) findPreference(PREFS_FILE_NAME);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Editor editor = sharedPref.edit();
		//editor.clear();
		//editor.commit();
        Set<String> set = sharedPref.getStringSet(KEY_PREF_MULTILIST_SITES, new HashSet<String>(Arrays.asList(FirstTimeActivity.websNames)));
        connectionPref.setValues(set);
        editor.commit();
        
        // Set the intent for the About preference (triggered when clicked)
        findPreference("about").setIntent(new Intent(getActivity(), AboutActivity.class));
        // Set the intent for the Legal Notices preference (triggered when clicked)
        findPreference("legal").setIntent(new Intent(getActivity(), LegalNoticesActivity.class));
    }

	/**
	 * Listens for a change in the preferences
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		System.out.println("GUEEEEE ON SHARED PREFERENCE CHANGED");
		if (key.equals(KEY_PREF_MULTILIST_SITES)) {
        	System.out.println("key multilist changed");
            MultiSelectListPreference connectionPref = (MultiSelectListPreference) findPreference(key);
            // Save the old values
            Editor editor = sharedPreferences.edit();
            editor.putStringSet("lastSelection", new HashSet<String>(Arrays.asList(FirstTimeActivity.websNames)));
            editor.commit();
            // Get the new values
            Set<String> set = sharedPreferences.getStringSet(key, null);
            // Set the new values on the list
    		FirstTimeActivity.websNames = set.toArray(new String[0]);
    		// Set the values on the shown list
            connectionPref.setValues(set);
            // Remove the events from the non selected
            Iterator<String> iter = set.iterator();
            while (iter.hasNext()) {
              System.out.println(iter.next());
            }
            Intent i = new Intent(getActivity(), DateActivity.class);
			startActivity(i);
			
        }
		
	}
	
	@Override
	public void onResume() {
	    super.onResume();
        System.out.println("ON Resume THE SETTINGS FRAGMENT");
	    // Set up a listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
	    super.onPause();
        System.out.println("ON Pause THE SETTINGS FRAGMENT");
	    // Unregister the listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}

}
