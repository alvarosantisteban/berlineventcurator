package com.alvarosantisteban.berlincurator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Creates the settings 
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	
	 //public static final String PREFS_FILE_NAME = "multilist_sites"; I think is not needed
	
	// -----------
	// PREFERENCES
	// -----------
	/**
	 * Preference for the multilist with the selection of sites (origin)
	 */
	public static final String KEY_PREF_MULTILIST_SITES = "multilist_sites";
	/**
	 * Preference for the multilist with the selection of tags for topic
	 */
	public static final String KEY_PREF_MULTILIST_TOPIC = "multilist_thema";
	/**
	 * Preference for the multilist with the selection of tags for type
	 */
	public static final String KEY_PREF_MULTILIST_TYPE = "multilist_type";
	/**
	 * Preference for the multilist with the last selection of sites, used to compare it with the actual and know which changed
	 */
	public static final String KEY_PREF_MULTILIST_LAST_SELECTION = "lastSelection";
	/**
	 * Preference for the list with possible organizations
	 */
	public static final String KEY_PREF_LIST_ORGANIZATIONS = "possible_organizations_list";
	 
	// -----------
	// CONSTANTS
	// -----------
	public static final String TYPE_ORGANIZATION = "By Type";
	public static final String TOPIC_ORGANIZATION = "By Topic";
	public static final String ORIGIN_ORGANIZATION = "By Origin";

	ListPreference organizationList; 
	MultiSelectListPreference topicMultiList;
	MultiSelectListPreference typeMultiList;
	MultiSelectListPreference originMultiList;
	
	Set<String> setOfSites;
	
	 /**
	  * Loads the preferences from the XML file
	  */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("ON CREATE THE SETTINGS FRAGMENT");
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        organizationList = (CustomListPreference) findPreference(KEY_PREF_LIST_ORGANIZATIONS); 
        topicMultiList = (MultiSelectListPreference) findPreference(KEY_PREF_MULTILIST_TOPIC);
        typeMultiList = (MultiSelectListPreference) findPreference(KEY_PREF_MULTILIST_TYPE);
        originMultiList = (MultiSelectListPreference) findPreference(KEY_PREF_MULTILIST_SITES);
        
        // Enable the active multilist and disable the other two 
        enableActiveOrganization(organizationList.getValue());
        
        // Get the set of active websites
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setOfSites = sharedPref.getStringSet(KEY_PREF_MULTILIST_SITES, new HashSet<String>(Arrays.asList(getResources().getStringArray(R.array.default_sites_array))));
        
        // Set the intent for the About preference (triggered when clicked)
        findPreference("about").setIntent(new Intent(getActivity(), AboutActivity.class));
        // Set the intent for the Legal Notices preference (triggered when clicked)
        findPreference("legal").setIntent(new Intent(getActivity(), LegalNoticesActivity.class));
        // Set the listener of the preference list of possible organizations
        organizationList.setOnPreferenceChangeListener(preferenceListener);        	
    }

	/**
	 * Listens for a change in the preferences
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		System.out.println("GUEEEEE ON SHARED PREFERENCE CHANGED");
		if (key.equals(KEY_PREF_MULTILIST_SITES)) {
        	System.out.println("key=multilist sites changed");
            // Save the old values
            Editor editor = sharedPreferences.edit();
            editor.putStringSet(KEY_PREF_MULTILIST_LAST_SELECTION, setOfSites);
            editor.commit();
            
            // Set the new values
            setOfSites = originMultiList.getValues();
            editor.putStringSet(KEY_PREF_MULTILIST_SITES, setOfSites);
            editor.commit();
            
            Intent i = new Intent(getActivity(), DateActivity.class);
			startActivity(i);	
        }else if (key.equals(KEY_PREF_MULTILIST_TYPE)){
        	System.out.println("key=multilist type changed");
        	Editor editor = sharedPreferences.edit();
            editor.putStringSet(KEY_PREF_MULTILIST_TYPE, typeMultiList.getValues());
            editor.commit();
        	// Go to Date Activity
            Intent i = new Intent(getActivity(), DateActivity.class);
			startActivity(i);
        }else if (key.equals(KEY_PREF_MULTILIST_TOPIC)){
        	System.out.println("key=multilist topic changed");
        	Editor editor = sharedPreferences.edit();
            editor.putStringSet(KEY_PREF_MULTILIST_TOPIC, topicMultiList.getValues());
            editor.commit();
        	// Go to Date Activity
            Intent i = new Intent(getActivity(), DateActivity.class);
			startActivity(i);
        }else if (key.equals(KEY_PREF_LIST_ORGANIZATIONS)){
        	System.out.println("key=possible_organizations_list changed");
        	String kindOfOrganization = sharedPreferences.getString(key, TYPE_ORGANIZATION);
        	if(kindOfOrganization.equals(TYPE_ORGANIZATION) || kindOfOrganization.equals(TOPIC_ORGANIZATION)){
        		System.out.println("Selected By Type or By Topic. Restore default websites selection");
        		restoreDefaultWebsitesSelection(sharedPreferences);
        	}
        }
	}

	private void restoreDefaultWebsitesSelection(SharedPreferences sharedPreferences) {		
		// Get the default sites selection
		Set<String> defaultSitesSelection = new HashSet<String>(Arrays.asList(getResources().getStringArray(R.array.default_sites_array)));
		// Set the values on the shown list
		originMultiList.setValues(defaultSitesSelection);
		// Save the new values
		Editor editor = sharedPreferences.edit();
		editor.putStringSet(KEY_PREF_MULTILIST_SITES, defaultSitesSelection);
		editor.commit();
		
		Intent i = new Intent(getActivity(), SettingsActivity.class);
		startActivity(i);
	}
	
	/**
	 * The listener for the changes of the preference list of possible organizations 
	 */
	private OnPreferenceChangeListener preferenceListener = new Preference.OnPreferenceChangeListener() {

  	  public boolean onPreferenceChange(Preference preference, Object newValue) {
  	    final String selectedOrganization = newValue.toString();
  	    enableActiveOrganization(selectedOrganization);
  	    return true;
  	  }
	};
  	
	/**
	 * Enables the active organization and disables the others
	 * 
	 * @param activeOrganization the key that identifies the active kind of organization
	 */
	private void enableActiveOrganization(String activeOrganization) {
		if (activeOrganization.equals(TYPE_ORGANIZATION)){
	    	topicMultiList.setEnabled(false);
	    	typeMultiList.setEnabled(true);
	    	originMultiList.setEnabled(false);
	    }else if(activeOrganization.equals(TOPIC_ORGANIZATION)){
	    	topicMultiList.setEnabled(true);
	    	typeMultiList.setEnabled(false);
	    	originMultiList.setEnabled(false);
	    }else{
	    	topicMultiList.setEnabled(false);
	    	typeMultiList.setEnabled(false);
	    	originMultiList.setEnabled(true);
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
	
	/**
	 * Prints out all preferences
	 * 
	 * @param sharedPref the shared preferences
	 */
	@SuppressWarnings("unused")
	private void printAllPreferences(SharedPreferences sharedPref) {
		Map<String,?> keys = sharedPref.getAll();

        System.out.println("------START--------");
        for(Map.Entry<String,?> entry : keys.entrySet()){
        	System.out.println("map values: " +entry.getKey() + ": " + entry.getValue().toString());            
         }
        System.out.println("-------END-------");
	}
}
