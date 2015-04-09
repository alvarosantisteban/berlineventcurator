package com.alvarosantisteban.pathos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alvarosantisteban.pathos.preferences.SettingsActivity;
import com.alvarosantisteban.pathos.preferences.SettingsFragment;
import com.alvarosantisteban.pathos.utils.Constants;
import com.alvarosantisteban.pathos.utils.DatabaseHelper;
import com.alvarosantisteban.pathos.utils.StringUtils;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.SelectArg;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Main activity that displays a list with the events for a selected day organized by the type, topic or origin of the website.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class DateActivity extends OrmLiteBaseActivity<DatabaseHelper>{

	//--------------------------------------------
	// CONSTANTS AND EXTRAS
	//--------------------------------------------
	
	// Constants to determinate the kind of organization
	private String TYPE_ORGANIZATION;
	private String TOPIC_ORGANIZATION;
	
	// Constants to access Preferences
	private final String LAST_SELECTION = "lastSelection";
	private final String FIRST_TIME_APP = "isFirstTimeApp";
	
	// The keyword passed as extra to EventActivity
	//public static final String EXTRA_EVENT = "com.alvarosantisteban.pathos.event";
	// The keyword received as extra to know the selected date 
	//private final String EXTRA_DATE = "date";
	// Settings
	private static final int RESULT_SETTINGS = 1;
	
	// Code return constants used to get info from Event Activity 
	private static final int INTENT_RETURN_CODE = 1;
	public static final int RESULT_UPDATE = 1;
	public static final String EVENTS_RESULT_DATA = "result data";
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "DateActivity";
	
	//--------------------------------------------
	// SETS OF TAGS
	//--------------------------------------------
	
	// The last selection of tags, to be compared with the actual one
	public Set<String> lastSelection;
	
	// The sets of tags for the different kinds of organizations
	public Set<String> typeTags;
	public Set<String> topicTags;
	public Set<String> originTags;
	
	// The array of Strings with the tags that is going to be used to load the events
	public String[] setOfTags;
	
	//--------------------------------------------
	// DATE RELATED
	//--------------------------------------------
	
	/**
	 * Format of the date
	 */
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
	
	/**
	 * The calendar used to get the actual date
	 */
	Calendar currentDay = Calendar.getInstance();
	
	/**
	 * The String with the date of today in the format DD/MM/YYYY
	 */
	private String today = dateFormat.format(currentDay.getTime());
	
	/**
	 * The TextView displaying the selected date. It may also display the words "Today" or "Tomorrow"
	 */
	private TextView displayedDate;
	
	/**
	 * The String with the chosenDate by the user in the format DD/MM/YYYY
	 */
	public String chosenDate;
	
	//--------------------------------------------
	// STRUCTURES
	//--------------------------------------------
	
	/**
	 * The expandable list with the groups and events
	 */
	ExpandableListView expandableSitesList;
	
	/**
	 * A LinkedHashMap with the name of the tag (from website/thema/type) as key and its corresponding HeaderInfo as value. 
	 * Used to make the search easier
	 */
	private LinkedHashMap<String, HeaderInfo> tagsMap = new LinkedHashMap<String, HeaderInfo>();
	
	/**
	 * An ArrayList with the HeaderInfo of each group
	 */
	private ArrayList<HeaderInfo> groupsList = new ArrayList<HeaderInfo>();
	 
	/**
	 * The adapter for the ExpandableListView
	 */
	private ListAdapter listAdapter;
	
	/**
	 * The position of the last touched group
	 */
	int lastTouchedGroupPosition;
	
	/**
	 * The position of the last child (event) touched inside  the lastTouchedGroupPosition
	 */
	int lastTouchedChildPosition;
	
	//--------------------------------------------
	// OTHER
	//--------------------------------------------
	
	private static Toast toast;
	
	/**
	 * The total number of events for a chosenDate
	 */
	int totalNumEvents = 0;
	
	final Context context = this;
	
	/**
   	 * The progress bar for downloading and extracting the events
   	 */
	ProgressBar loadProgressBar;
	
	/**
	 *  User preferences
	 */
	SharedPreferences sharedPref;
	
	/**
	 *  Loads the elements from the resources, gets the data from the mainActitivy and calls the parsers to extract the information that
	 *  will be shown.
	 *  
	 * @param savedInstanceState
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		
		TYPE_ORGANIZATION = getResources().getString(R.string.organization_by_type);
		TOPIC_ORGANIZATION = getResources().getString(R.string.organization_by_topic);
		toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		
		// Get the default shared preferences
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		// --------------------------------------------------
		// Know if its the first time the user uses the app
		// --------------------------------------------------
		boolean isFirstTimeApp = sharedPref.getBoolean(FIRST_TIME_APP, true);
		Intent intent;
		if (isFirstTimeApp) {
			// Go directly to First Time Activity
			intent = new Intent(context, FirstTimeActivity.class);
			startActivity(intent);
		}
		
		// --------------------------------------------------
		// Set basic UI
		// --------------------------------------------------
		
		setContentView(R.layout.activity_date);
		expandableSitesList = (ExpandableListView) findViewById(R.id.expandableSitesList);
		loadProgressBar = (ProgressBar)findViewById(R.id.progressLoadHtml);	
		
		// Get the old selection of tags
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			lastSelection = sharedPref.getStringSet(LAST_SELECTION, new HashSet<String>(Arrays.asList(context.getResources().getStringArray(R.array.default_sites_array))));
		} else {
			String s = sharedPref.getString(LAST_SELECTION, context.getResources().getString(R.string.sites_pseudoarray_values));
			if(s != null){
				lastSelection = new HashSet<String>(Arrays.asList(s.split(",")));
			}
		}
		// Get the sites are meant to be shown
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			originTags = sharedPref.getStringSet(SettingsFragment.KEY_PREF_MULTILIST_SITES, new HashSet<String>(Arrays.asList(context.getResources().getStringArray(R.array.default_sites_array))));
		} else {
			String s = sharedPref.getString(SettingsFragment.KEY_PREF_MULTILIST_SITES, context.getResources().getString(R.string.sites_pseudoarray_values));
			if(s != null){
				originTags = new HashSet<String>(Arrays.asList(s.split(",")));
			}
		}
		
		// Get the kind of organization
		String kindOfOrganization = sharedPref.getString(SettingsFragment.KEY_PREF_LIST_ORGANIZATIONS, TYPE_ORGANIZATION);
		String kindOfOrganizationDBTag;
		if (kindOfOrganization.equals(TYPE_ORGANIZATION)){
			// Get the set of type tags
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				typeTags = sharedPref.getStringSet(SettingsFragment.KEY_PREF_MULTILIST_TYPE, new HashSet<String>(Arrays.asList(context.getResources().getStringArray(R.array.types_array_values))));
			} else {
				String s = sharedPref.getString(SettingsFragment.KEY_PREF_MULTILIST_TYPE, context.getResources().getString(R.string.types_pseudoarray_values));
				if(s != null){
					typeTags = new HashSet<String>(Arrays.asList(s.split(",")));
				}
			}
			setOfTags = typeTags.toArray(new String[0]);
			kindOfOrganizationDBTag = "typeTag";
		}else if (kindOfOrganization.equals(TOPIC_ORGANIZATION)){
			// Get the set of topic tags
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				topicTags = sharedPref.getStringSet(SettingsFragment.KEY_PREF_MULTILIST_TOPIC, new HashSet<String>(Arrays.asList(context.getResources().getStringArray(R.array.themas_array_values))));
			} else {
				String s = sharedPref.getString(SettingsFragment.KEY_PREF_MULTILIST_TOPIC, context.getResources().getString(R.string.topics_pseudoarray_values));
				if(s != null){
					topicTags = new HashSet<String>(Arrays.asList(s.split(",")));
				}
			}
			
			setOfTags = topicTags.toArray(new String[0]);
			kindOfOrganizationDBTag ="themaTag";
		}else{
			setOfTags = originTags.toArray(new String[0]);
			kindOfOrganizationDBTag = "eventsOrigin";
		}
		
		// Create the header groups
		createHeaderGroups(setOfTags);
		 
	    // --------------------------------------------------
	 	// Display the right date
	    // --------------------------------------------------
		
		intent = getIntent();
		// Get the choosen date from the calendar
		chosenDate = intent.getStringExtra(Constants.EXTRA_DATE);
		if (chosenDate == null){
			// The user did not select anything, the default date is today
			chosenDate = sharedPref.getString(Constants.CHOSEN_DATE, today);
		}else{
			// Save the date selection
			Editor editor = sharedPref.edit();
			editor.putString(Constants.CHOSEN_DATE, chosenDate);
			editor.commit();
		}	
		displayedDate = (TextView) findViewById(R.id.date);
		if (chosenDate.equals(today)){
			displayedDate.setText(R.string.events_for_today);
		}else if (chosenDate.equals(getTomorrow())){
			displayedDate.setText(R.string.events_for_tomorrow);
		}else{
			displayedDate.setText(R.string.events_for_a);
			displayedDate.append(" " + chosenDate);
		}
		
		// --------------------------------------------------
		// Adapter and loading of events
		// --------------------------------------------------
		
		// Create the adapter by passing the ArrayList data
		listAdapter = new ListAdapter(DateActivity.this, groupsList);
		// Attach the adapter to the expandableList
		expandableSitesList.setAdapter(listAdapter);
		
		// Load the events for the selected websites and tags
		loadEvents(kindOfOrganizationDBTag, setOfTags);
		
		// Expand the groups with events
		expandGroupsWithEvents();
		
		// Listener for the events
		expandableSitesList.setOnChildClickListener(myEventClicked);
		// Listener for the sites
		expandableSitesList.setOnGroupClickListener(myListGroupClicked);
		
		// Listener for the collapsed group
		//expandableSitesList.setOnGroupCollapseListener(myCollapsedGroup);
		// Listener for the expanded group
		//expandableSitesList.setOnGroupExpandListener(myExpandedGroup);
		
		// Get the number of events
		totalNumEvents = listAdapter.getTotalChildrenCount();
		if(totalNumEvents == 0 && !isFirstTimeApp){
			displayToast("There are no events for this day. Refresh or go to another day.");
		}
		
		// Check if there were changes in the selection of the groups
		if(!lastSelection.equals(originTags)){
			Log.d(TAG, "The user changed some of the tags.");
			Set<String> deletedGroups = new HashSet<String>(lastSelection);
			Set<String> addedGroups = new HashSet<String>(originTags);
			deletedGroups.removeAll(originTags);
			addedGroups.removeAll(lastSelection);
			
			// Get the events for the new groups
			if(addedGroups.size() > 0){
				downloadEventsForAddedGroups(addedGroups);
			}
			
			// Delete the events for the groups not used anymore
			if(deletedGroups.size() > 0){
				removeGroupsFromDB(deletedGroups);
			}
			
			// Save the new selection
			lastSelection = originTags;
			Editor editor = sharedPref.edit();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				editor.putStringSet(LAST_SELECTION, lastSelection);
			} else {
				editor.putString(LAST_SELECTION, StringUtils.join(lastSelection, ","));
			}
            editor.commit();
		}
	}
	
	// ----------------------------------------------------------------------------------------------
	// ADD AND DELETE EVENTS
	// ----------------------------------------------------------------------------------------------

	/**
	 * Removes the events from the DB that match the origin of any of the String in the set
	 * 
	 * @param deletedGroups Set with the Strings of the origins to be deleted
	 */
	private void removeGroupsFromDB(Set<String> deletedGroups) {
		Log.d(TAG, "There are " +deletedGroups.size() +" groups deleted");
		// Get the Event DAO to deleted the entries of the unselected groups
		RuntimeExceptionDao<Event, Integer> eventDao = getHelper().getEventDataDao();
		DeleteBuilder<Event, Integer> deleteBuilder = eventDao.deleteBuilder();	
		Iterator <String> deletedIterator = deletedGroups.iterator();
		while (deletedIterator.hasNext()){
			try {
				// create our argument which uses a SQL ? to avoid having problems with apostrophes
				SelectArg deletedArg = new SelectArg(deletedIterator.next());
				deleteBuilder.where().eq("eventsOrigin", deletedArg);
				deleteBuilder.delete();
			} catch (SQLException e) {
				Log.e(TAG, "Error deleting events after checking difference between old and new selection." +e);
			}
		}
	}

	/**
	 * Downloads the events that match the origin of any of the String in the set
	 * 
	 * @param addedGroups Set with the Strings of the origins to be added
	 */
	private void downloadEventsForAddedGroups(Set<String> addedGroups) {
		Log.d(TAG, "There are " +addedGroups.size() +" new groups added");
		// Create a connection
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		// Check if is possible to establish a connection
		if (networkInfo != null && networkInfo.isConnected()) {
			DownloadWebpageAsyncTask download = new DownloadWebpageAsyncTask(this, loadProgressBar, chosenDate, context.getString(R.string.searching));
			// Execute the asyncronous task of downloading the websites
			download.execute(addedGroups.toArray((new String[addedGroups.size()])));
		}
	}	
	
	/**
	 * Loads the events from the DB that match the chosenDate and the set of tags for the kind of organization passed as parameters
	 * 
	 * @param kindOfOrganization the kind of organization (By Type, Thema, Origin)
	 * @param setOfTags the set of tags that the event has to match in order to be extracted from the DB
	 */
	private void loadEvents(String kindOfOrganization, String[] setOfTags){ 
		Log.d(TAG,"LOAD EVENTS");
		// Get our dao
		RuntimeExceptionDao<Event, Integer> eventDao = getHelper().getEventDataDao();
		for (int i=0; i<setOfTags.length; i++){
			Log.d(TAG,setOfTags[i]);
			List<Event> eventsFromWebsite = null;
			try {
				Map<String, Object> fieldValues = new HashMap<String,Object>();
				fieldValues.put(kindOfOrganization, setOfTags[i]);
				fieldValues.put("day", chosenDate);
				eventsFromWebsite = eventDao.queryForFieldValuesArgs(fieldValues);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG,"DB exception while retrieving the events from the website " +setOfTags[i]);
			}
			// Add the events to the corresponding group
			for (int j = 0; j < eventsFromWebsite.size(); j++) {
				addEvent(setOfTags[i],eventsFromWebsite.get(j));
			}
		}
	}
	
	/**
	 * Add an event to the list of events of its corresponding group 
	 * 
	 * @param theGroupTag The tag to whom the the event belongs
	 * @param newEvent The event to be attached
	 * @return the position of the group where the event was added
	 */
	private int addEvent(String theGroupTag, Event newEvent){	   
		// Check in the hash map if the group already exists
		HeaderInfo headerInfo = tagsMap.get(theGroupTag);
		
		//  Create the group if doesn't exists
		if(headerInfo == null){
			headerInfo = new HeaderInfo();
			headerInfo.setName(theGroupTag);
			tagsMap.put(theGroupTag, headerInfo);
			groupsList.add(headerInfo);
		}
	 
		// Get the children (events) for the group
		ArrayList<Event> eventsList = headerInfo.getEventsList();
		// Get the size of the children list
		int listSize = eventsList.size();
		// Add to the counters
		listSize++;
	 
		// Set the sequence for the event
		newEvent.setSequence(String.valueOf(listSize));
		// Add the event to the list of events
		eventsList.add(newEvent);
		// Update the site with the "new" eventsList
		headerInfo.setEventsList(eventsList);
		headerInfo.setEventsNumber(listSize);
		
		// find the group position inside the list
		return groupsList.indexOf(headerInfo);
	}
	
	/**
	 * Populates the tagsMap and the groupsList by creating {@link HeaderInfo} using the array of tags passed
	 * in the parameter.
	 * 
	 * @param tagsNames the set of tags used to classified events
	 */
	private void createHeaderGroups(String[] tagsNames) {
		for (int i=0; i<tagsNames.length; i++){
			HeaderInfo headerInfo = new HeaderInfo();
			headerInfo.setName(tagsNames[i]);
			tagsMap.put(tagsNames[i], headerInfo);
			groupsList.add(headerInfo);
		}
	}
	
	// ----------------------------------------------------------------------------------------------
	// CLICK LISTENERS
	// ----------------------------------------------------------------------------------------------
	
	/**
	 * The child listener that detects the click on an event from the list and goes to the corresponding EventActivity.
	 */
	private OnChildClickListener myEventClicked =  new OnChildClickListener() {
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
			// Get the group header 
			HeaderInfo headerInfo = groupsList.get(groupPosition);
			// Get the child info
			Event clickedEvent =  headerInfo.getEventsList().get(childPosition);
			// Update the last touched group and child position
			lastTouchedGroupPosition = groupPosition;
			lastTouchedChildPosition = childPosition;
			// Go the Event Activity
			Intent intent = new Intent(context, EventActivity.class);
			intent.putExtra(Constants.EXTRA_EVENT, clickedEvent);
			startActivityForResult(intent, INTENT_RETURN_CODE);
			return false;
		}
	};
	
	/**
	 * The group listener that detects the click on the group header
	 */
	private OnGroupClickListener myListGroupClicked =  new OnGroupClickListener() { 
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			// Get the group header
			HeaderInfo headerInfo = groupsList.get(groupPosition);
			// If the group does not contain events, tell the user
			if(headerInfo.getEventsNumber() == 0){
				displayToast("There are no events to show for " + headerInfo.getName());
				// Avoid propagation = the group is not expanded/collapsed
				return true;
			}
			return false;
		} 
	};
	
	// ----------------------------------------------------------------------------------------------
	// RELATED TO THE MENU
	// ----------------------------------------------------------------------------------------------
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.date, menu);
		return true;
	}
	
	/**
	 * Checks which item from the menu has been clicked
	 */
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_map) {
        	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				Intent i2 = new Intent(this, MapActivity.class);
				startActivity(i2);
        	}else{
    			Intent i2 = new Intent(this, FakeCalendarActivity.class);
				startActivity(i2);
        	}
		}else if (item.getItemId() == R.id.menu_calendar) {
        	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				Intent i2 = new Intent(this, CalendarActivity.class);
				startActivity(i2);
        	}else{
    			Intent i2 = new Intent(this, FakeCalendarActivity.class);
				startActivity(i2);
        	}
		}else if (item.getItemId() == R.id.menu_refresh_events) {
			// Create a connection
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		    // Check if is possible to establish a connection
		    if (networkInfo != null && networkInfo.isConnected()) {
		    	DownloadWebpageAsyncTask download = new DownloadWebpageAsyncTask(this,loadProgressBar, chosenDate, context.getString(R.string.searching));
				// Execute the asyncronous task of downloading the websites
				download.execute(originTags.toArray(new String[0]));
		    } else {
		    	// Inform the user that there is no network connection available
		    	displayToast(getString(R.string.no_network));
		        Log.w(TAG,getString(R.string.no_network));
		    }
		}else if (item.getItemId() == R.id.menu_settings) {
			Intent i = new Intent(this, SettingsActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
		}
        return true;
    }
	
	
	// ----------------------------------------------------------------------------------------------
	// LIFECYCLE
	// ----------------------------------------------------------------------------------------------
	
	public void onStart() {
		super.onStart();
		Log.d(TAG,"In the onStart() event");
	}		   

	public void onRestart() {
		super.onRestart();
		Log.d(TAG, "In the onRestart() event");
	}
	    
	public void onResume() {
		super.onResume();
		Log.d(TAG,"In the onResume() event");
	}
	    
	public void onPause() {
	    super.onPause();
	    Log.d(TAG,"In the onPause() event");
	}
	    
	public void onStop() {
	    super.onStop();
	    Log.d(TAG, "In the onStop() event");
	}
	    
	public void onDestroy() {
	    super.onDestroy();
	    Log.d(TAG, "In the onDestroy() event");
	}
	
	/**
	 * Called when an activity you launched exits, giving you the requestCode you started it with, 
	 * the resultCode it returned, and any additional data from it. 
	 * The resultCode will be RESULT_CANCELED if the activity explicitly returned that, didn't return any result, 
	 * or crashed during its operation.
	 * 
	 * You will receive this call immediately before onResume() when your activity is re-starting.
	 * 
	 * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
	 * @param resultCode The integer result code returned by the child activity through its setResult().
	 * @param data 	An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "In the onActivityResult() event");
	    
		if (requestCode == INTENT_RETURN_CODE) {
			if(resultCode == RESULT_UPDATE){      
		        // The event was updated
				//Event changedEvent = (Event)data.getSerializableExtra(EVENTS_RESULT_DATA);
				Event changedEvent = (Event)data.getParcelableExtra(EVENTS_RESULT_DATA);
				replaceAnEvent(changedEvent);
				listAdapter.notifyDataSetChanged();
		    }
		}
	}
	
	/**
	 * Replaces the event pointed by lastTouchedGroupPosition and lastTouchedChildPosition for the one received as parameter
	 * 
	 * @param changedEvent the event that will be set in the websitesList
	 */
	private void replaceAnEvent(Event changedEvent) {		
		// Get the group header of the touched event
		HeaderInfo headerInfo = groupsList.get(lastTouchedGroupPosition);
		// Substitute the old event for the new one
		headerInfo.getEventsList().set(lastTouchedChildPosition, changedEvent);
		groupsList.set(lastTouchedGroupPosition, headerInfo);
	}
	
	// ----------------------------------------------------------------------------------------------
	// OTHER
	// ----------------------------------------------------------------------------------------------
	
	/**
	 * Displays the message passed as parameter. 
	 * By calling this method, the toast object is updated without having to wait to the end of the last toast displayed.
	 * 
	 * @param message the message to be displayed
	 */
	private void displayToast(final String message) {
	    //toast.cancel();
	    toast.setText(message); 
	    toast.setGravity(Gravity.TOP, 0, FirstTimeActivity.actionBarHeight);
	    toast.show();
	}	
	
	/**
	 * Returns the date of tomorrow formated like DD/MM/YYYY
	 * 
	 * @return a String with tomorrow in the format DD/MM/YYYY
	 */
	private String getTomorrow() {
		Calendar tomorrow = currentDay;
		// add one day to the date/calendar
	    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		return dateFormat.format(tomorrow.getTime());
	}
	
	/**
	 * Expand all groups
	 */
	@SuppressWarnings("unused")
	private void expandAll() {
		int count = listAdapter.getGroupCount();
		for (int i = 0; i < count; i++){
			expandableSitesList.expandGroup(i);
		}
	}
	
	/**
	 * Collapse all groups
	 */
	@SuppressWarnings("unused")
	private void collapseAll() {
		int count = listAdapter.getGroupCount();
		for (int i = 0; i < count; i++){
			expandableSitesList.collapseGroup(i);
		}
	}
	
	/**
	 * Expand the groups with events on it
	 */
	private void expandGroupsWithEvents(){
		int count = listAdapter.getGroupCount();
		for (int i = 0; i < count; i++){
			if(listAdapter.getChildrenCount(i) > 0){
				expandableSitesList.expandGroup(i);
			}
		}
	}
}
