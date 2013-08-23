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
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.ExpandableListView.OnChildClickListener;

/**
 * Creates the settings 
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	 //public static final String PREFS_FILE_NAME = "multilist_sites"; I think is not needed
	 public static final String KEY_PREF_MULTILIST_SITES = "multilist_sites";
	 public static final String KEY_PREF_MULTILIST_TOPIC = "multilist_thema";
	 public static final String KEY_PREF_MULTILIST_TYPE = "multilist_type";
	 public static final String KEY_PREF_MULTILIST_LAST_SELECTION = "lastSelection";
	 public static final String KEY_PREF_LIST_ORGANIZATIONS = "possible_organizations_list";
	 
	 public static final String KEY_PREF_TYPE = "By Type";
	 public static final String KEY_PREF_TOPIC = "By Topic";
	 public static final String KEY_PREF_ORIGIN = "By Origin";
	 
	 //Set<String> selectedWebsites;

	 ListPreference organizationList; 
	 MultiSelectListPreference topicList;
	 MultiSelectListPreference typeList;
	 MultiSelectListPreference originList;
	
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
        topicList = (MultiSelectListPreference) findPreference(KEY_PREF_MULTILIST_TOPIC);
        typeList = (MultiSelectListPreference) findPreference(KEY_PREF_MULTILIST_TYPE);
        originList = (MultiSelectListPreference) findPreference(KEY_PREF_MULTILIST_SITES);
        
        // Check which organization is the active one.
        String activeOrganization = organizationList.getValue();
        // Enable the active multilist and disable the other two 
        enableActiveOrganization(activeOrganization);
        
        // Get the set of active websites
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> set = sharedPref.getStringSet(KEY_PREF_MULTILIST_SITES, new HashSet<String>(Arrays.asList(FirstTimeActivity.websNames)));
        originList.setValues(set);
        
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
            editor.putStringSet(KEY_PREF_MULTILIST_LAST_SELECTION, new HashSet<String>(Arrays.asList(FirstTimeActivity.websNames)));
            editor.commit();
            // Get the new values
            Set<String> set = sharedPreferences.getStringSet(key, null);
            // Set the new values on the list
    		FirstTimeActivity.websNames = set.toArray(new String[0]);
    		// Set the values on the shown list
            originList.setValues(set);
            // Go to Date Activity
            Intent i = new Intent(getActivity(), DateActivity.class);
			startActivity(i);
			
        }else if (key.equals(KEY_PREF_LIST_ORGANIZATIONS)){
        	System.out.println("key=possible_organizations_list changed");
        	String kindOfOrganization = sharedPreferences.getString(key, KEY_PREF_TYPE);
        	if(kindOfOrganization.equals(KEY_PREF_TYPE)){
        		System.out.println("Selected By Type");
        		restoreDefaultWebsitesSelection(sharedPreferences);
        	}else if(kindOfOrganization.equals(KEY_PREF_TOPIC)){
        		System.out.println("Selected By Topic");
        		restoreDefaultWebsitesSelection(sharedPreferences);
        	}else if(kindOfOrganization.equals(KEY_PREF_ORIGIN)){
        		System.out.println("Selected By Origin");
        	}
        }else if (key.equals(KEY_PREF_MULTILIST_TYPE)){
        	System.out.println("key=multilist type changed");
        	// Go to Date Activity
            Intent i = new Intent(getActivity(), DateActivity.class);
			startActivity(i);
        }else if (key.equals(KEY_PREF_MULTILIST_TOPIC)){
        	System.out.println("key=multilist topic changed");
        	// Go to Date Activity
            Intent i = new Intent(getActivity(), DateActivity.class);
			startActivity(i);
        }
	}

	private void restoreDefaultWebsitesSelection(SharedPreferences sharedPreferences) {
		// Save old values
		Editor editor = sharedPreferences.edit();
		editor.putStringSet(KEY_PREF_MULTILIST_LAST_SELECTION, new HashSet<String>(Arrays.asList(FirstTimeActivity.websNames)));
		// Save new values
		editor.putStringSet(KEY_PREF_MULTILIST_SITES, new HashSet<String>(Arrays.asList(getResources().getStringArray(R.array.default_sites_array))));
		editor.commit();
		
		// Set all origin values on the list of websites
		FirstTimeActivity.websNames = getResources().getStringArray(R.array.default_sites_array);
		
		// Set the values on the shown list
		Set<String> set = sharedPreferences.getStringSet(KEY_PREF_MULTILIST_SITES, null);
		originList.setValues(set);
		
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
		if (activeOrganization.equals(KEY_PREF_TYPE)){
	    	topicList.setEnabled(false);
	    	typeList.setEnabled(true);
	    	originList.setEnabled(false);
	    }else if(activeOrganization.equals(KEY_PREF_TOPIC)){
	    	topicList.setEnabled(true);
	    	typeList.setEnabled(false);
	    	originList.setEnabled(false);
	    }else{
	    	topicList.setEnabled(false);
	    	typeList.setEnabled(false);
	    	originList.setEnabled(true);
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
