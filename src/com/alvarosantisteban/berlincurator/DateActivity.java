package com.alvarosantisteban.berlincurator;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Displays a list with the events for a concrete day organized by the origin of the website and the time.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class DateActivity extends OrmLiteBaseActivity<DatabaseHelper>{

	public static final String EXTRA_EVENT = "com.alvarosantisteban.berlincurator.event";
	// Settings
	private static final int RESULT_SETTINGS = 1;
	
	//private boolean isToday=false;
	//private boolean isTomorrow =false;
			
	/**
	 * Used for logging purposes
	 */
	String tag = "DateActivity";
	
	/**
	 * Format of the date
	 */
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
	
	/**
	 * The calendar used to get the actual date
	 */
	Calendar currentDay = Calendar.getInstance();
	
	/**
	 * The TextView displaying the selected date. It may also display the words "Today" or "Tomorrow"
	 */
	private TextView displayedDate;
	
	/**
	 * The String with the date of today in the format DD/MM/YYYY
	 */
	private String today = dateFormat.format(currentDay.getTime());
	
	/**
	 * The String with the choosenDate by the user in the format DD/MM/YYYY
	 */
	public String choosenDate;
	
	/**
	 * The expandable list with the groups and events
	 */
	ExpandableListView expandableSitesList;
	
	/**
	 * A LinkedHashMap with the a String as key and a HeaderInfo as value
	 */
	private LinkedHashMap<String, HeaderInfo> websites = new LinkedHashMap<String, HeaderInfo>();
	/**
	 * An ArrayList with the HeaderInfo of each website
	 */
	private ArrayList<HeaderInfo> websitesList = new ArrayList<HeaderInfo>();
	 
	/**
	 * The adapter for the ExpandableListView
	 */
	private ListAdapter listAdapter;
	
	/**
	 * The Database Helper that helps dealing with the db easily
	 */
	//private DatabaseHelper databaseHelper = null;
	
	final Context context = this;
		
	/**
	 *  Loads the elements from the resources, gets the data from the mainActitivy and calls the parsers to extract the information that
	 *  will be shown.
	 *  
	 * @param savedInstanceState
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		System.out.println(tag +"onCreate-----------------");
		
		setContentView(R.layout.activity_date);
		
		// Enable the app's icon to act as home
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		//eventsList = intent.getExtra(MainActivity.EXTRA_HTML);
		// Get the choosen date from the calendar or from the event that the user was consulting
		choosenDate = intent.getStringExtra(CalendarActivity.EXTRA_DATE);
		choosenDate = intent.getStringExtra(EventActivity.EXTRA_DATE);
		displayedDate = (TextView) findViewById(R.id.date);
		if (choosenDate == null){	
			// The user did not select anything, the default date is today
			displayedDate.setText("These are the events for today!");
			choosenDate = today;
		}else{
			if (choosenDate.equals(today)){
				displayedDate.setText("These are the events for today!");
			}else if (choosenDate.equals(getTomorrow())){
				displayedDate.setText("These are the events for tomorrow");
			}else{
				displayedDate.setText("Events for the " +choosenDate);
			}
		}
		
		createHeaderGroups(MainActivity.websNames);
		
		//databaseHelper = getHelper();
		
		expandableSitesList = (ExpandableListView) findViewById(R.id.expandableSitesList);
		// Create the adapter by passing the ArrayList data
		listAdapter = new ListAdapter(DateActivity.this, websitesList);
		// Attach the adapter to the expandableList
		expandableSitesList.setAdapter(listAdapter);
		
		// Load the events for the selected websites
		loadEvents();
		
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
	private void expandAll() {
		int count = listAdapter.getGroupCount();
		for (int i = 0; i < count; i++){
			expandableSitesList.expandGroup(i);
		}
	}
	
	/**
	 * Collapse all groups
	 */
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
			/*else{
				expandableSitesList.setGroupIndicator(null);
			}*/
		}
	}
	
	/**
	 * Loads the events from the selected websites into the list if the day is the right one
	 *
	private void loadEvents(){ 
		// Load the events for the selected websites
		Set <Entry<String, List<Event>>> keyValue = MainActivity.events.entrySet();
		Iterator<Entry<String, List<Event>>> keyValueIterator = keyValue.iterator();
		while(keyValueIterator.hasNext()){
			Entry<String, List<Event>> entry = keyValueIterator.next();
			List<Event> eventsList = entry.getValue();
			for (int i=0; i<eventsList.size();i++){
				if(eventsList.get(i).getDay().equals(choosenDate)){
					//addEvent(entry.getKey(), eventsList.get(i));
					addEvent(eventsList.get(i).getEventsOrigin(), eventsList.get(i));
				}
			}
		}
	}*/
	
	/**
	 * Loads the events from the selected websites into the list if the day is the right one
	 */
	private void loadEvents(){ 
		/*
		// Load the events for the selected websites
		Set <Entry<String, List<Event>>> keyValue = MainActivity.events.entrySet();
		Iterator<Entry<String, List<Event>>> keyValueIterator = keyValue.iterator();
		while(keyValueIterator.hasNext()){
			Entry<String, List<Event>> entry = keyValueIterator.next();
			List<Event> eventsList = entry.getValue();
			for (int i=0; i<eventsList.size();i++){
				if(eventsList.get(i).getDay().equals(choosenDate)){
					//addEvent(entry.getKey(), eventsList.get(i));
					addEvent(eventsList.get(i).getEventsOrigin(), eventsList.get(i));
				}
			}
		}*/
		System.out.println("LOAD EVENTS");
		System.out.println("-----------------------------");
		// Get our dao
		RuntimeExceptionDao<Event, Integer> eventDao = getHelper().getEventDataDao();
		for (int i=0; i<MainActivity.websNames.length; i++){
		//int i=0;
			System.out.println(MainActivity.websNames[i]);
			List<Event> eventsFromWebsite = null;
			try {
				//eventsFromWebsite = eventDao.queryBuilder().where().eq("eventsOrigin",MainActivity.websNames[i]).query();
				//eventsFromWebsite = eventDao.queryBuilder().where().eq("eventsOrigin","White Trash").query();
				//eventsFromWebsite = eventDao.queryForAll();
				Map<String, Object> fieldValues = new HashMap<String,Object>();
				fieldValues.put("eventsOrigin", MainActivity.websNames[i]);
				//eventsFromWebsite = eventDao.queryForEq("eventsOrigin", MainActivity.websNames[i]);
				eventsFromWebsite = eventDao.queryForFieldValuesArgs(fieldValues);
			//} catch (SQLException e) {
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("DB exception while retrieving the events from the website " +MainActivity.websNames[i]);
			}
			System.out.println("eventsFromWebsite.size():" +eventsFromWebsite.size());
			for (int j = 0; j < eventsFromWebsite.size(); j++) {
				//System.out.println(eventsFromWebsite.get(j).getName());
				if(eventsFromWebsite.get(j).getDay().equals(choosenDate)){
					addEvent(MainActivity.websNames[i],eventsFromWebsite.get(j));
				}
			}
			//System.out.println("------------\\-----------------");
		}
		//System.out.println("-----------------------------");
	}
	
	/**
	 * Add an event to its corresponding group (site where it comes from)
	 * 
	 * @param websiteName String with the name of the website from where the event comes from
	 * @param newEvent The event to be attached
	 * @return the position of group where the event was added
	 */
	private int addEvent(String websiteName, Event newEvent){
		int groupPosition = 0;
	   
		// Check in the hash map if the group already exists
		HeaderInfo headerInfo = websites.get(websiteName);
		// Add the group if doesn't exists
		if(headerInfo == null){
			headerInfo = new HeaderInfo();
			headerInfo.setName(websiteName);
			websites.put(websiteName, headerInfo);
			websitesList.add(headerInfo);
		}
	 
		// Get the children (events) for the group
		ArrayList<Event> eventsList = headerInfo.getEventsList();
		// Get the size of the children list
		int listSize = eventsList.size();
		// Add to the counter
		listSize++;
	 
		// Set the sequence for the event
		newEvent.setSequence(String.valueOf(listSize));
		// Add it to its list of events
		eventsList.add(newEvent);
		// Update the site with the "new" eventsList
		headerInfo.setEventsList(eventsList);
		headerInfo.setEventsNumber(listSize);
		 
		//find the group position inside the list
		groupPosition = websitesList.indexOf(headerInfo);
		return groupPosition;
	}
	
	/**
	 * The child listener for the events
	 */
	private OnChildClickListener myEventClicked =  new OnChildClickListener() {
		 
		  public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
	    
			  // Get the group header 
			  HeaderInfo headerInfo = websitesList.get(groupPosition);
			  // Get the child info
			  Event clickedEvent =  headerInfo.getEventsList().get(childPosition);
			  // Display it or do something with it
			  //Toast.makeText(getBaseContext(), "Clicked on Detail " + headerInfo.getName() + "/" + clickedEvent.getName(), Toast.LENGTH_LONG).show();
			  Intent intent = new Intent(context, EventActivity.class);
			  intent.putExtra(EXTRA_EVENT, clickedEvent);
			  startActivity(intent);
			  return false;
		  }
	};
	
	/**
	 * The group listener for the sites
	 */
	private OnGroupClickListener myListGroupClicked =  new OnGroupClickListener() {
		 
		  public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			  System.out.println("onGroupClick");
			  // Get the group header
			  HeaderInfo headerInfo = websitesList.get(groupPosition);
			  // If the group does not contain events, tell the user
			  if(headerInfo.getEventsNumber() == 0){
				  Toast toast = Toast.makeText(getBaseContext(), "There are no events to show for " + headerInfo.getName(), Toast.LENGTH_SHORT);
				  toast.setGravity(Gravity.TOP, 0, MainActivity.actionBarHeight);
				  toast.show();
				  // Avoid propagation = the group is not expanded/collapsed
				  return true;
			  }
			  return false;
		  }
	   
	};
	
	/**
	 * The group collapse listener 
	 */
	private OnGroupCollapseListener myCollapsedGroup = new OnGroupCollapseListener(){
		
		public void onGroupCollapse(int groupPosition){
			System.out.println("onGroupCollapse");
		}
	};
	
	/**
	 * The group expand listener
	 */
	private OnGroupExpandListener myExpandedGroup = new OnGroupExpandListener(){
		
		public void onGroupExpand(int groupPosition){
			System.out.println("onGroupExpand");
		}
	};
	
	/**
	 * Populates the {@link websites} and the {@link websitesList} by creating {@link HeaderInfo} using the array of names of sites passed
	 * in the parameter.
	 * 
	 * @param sitesNames the names of the sites which contain the events
	 */
	private void createHeaderGroups(String[] sitesNames) {
		//System.out.println("createHeaderGroups");
		for (int i=0; i<sitesNames.length; i++){
			HeaderInfo headerInfo = new HeaderInfo();
			headerInfo.setName(sitesNames[i]);
			websites.put(sitesNames[i], headerInfo);
			websitesList.add(headerInfo);
		}
	}
	
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
        if (item.getItemId() == R.id.menu_calendar) {
			Intent i2 = new Intent(this, CalendarActivity.class);
			startActivity(i2);
		} else if (item.getItemId() == android.R.id.home) {
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
 
        return true;
    }
	
	public void onStart() {
		super.onStart();
		System.out.println(tag +"In the onStart() event");
	}		   

	public void onRestart() {
		super.onRestart();
	    System.out.println(tag + "In the onRestart() event");
	}
	    
	public void onResume() {
		super.onResume();
	    System.out.println(tag +"In the onResume() event");
	}
	    
	public void onPause() {
	    super.onPause();
	    System.out.println(tag + "In the onPause() event");
	}
	    
	public void onStop() {
	    super.onStop();
	    System.out.println(tag + "In the onStop() event");
	}
	    
	public void onDestroy() {
	    super.onDestroy();
	    System.out.println(tag + "In the onDestroy() event");
	    /*
	    if (databaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        databaseHelper = null;
	    }*/
	}
	
	/*
	private DatabaseHelper getHelper() {
	    if (databaseHelper == null) {
	        databaseHelper =
	            OpenHelperManager.getHelper(this, DatabaseHelper.class);
	    }
	    return databaseHelper;
	}*/
}
