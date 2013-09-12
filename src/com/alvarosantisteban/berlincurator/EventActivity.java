package com.alvarosantisteban.berlincurator;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.ActionBar;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alvarosantisteban.berlincurator.utils.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;


/**
 * Displays all the information of an event: Day, name, hour, description, origin, tags, links and map.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class EventActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	
	CustomScrollView scrollView;
	LinearLayout eventLayout;
	TextView name;
	TextView day;
	TextView time;
	TextView link;
	TextView description;
	TextView origin;
	TextView location;
	CheckBox interestingCheck;
	
	Geocoder geocoder = null;  
	MapView mapita;
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "EventActivity";
	
	/**
	 * The event being displayed
	 */
	Event event;
	/**
	 *  The key word passed as extra from DateActivity
	 */
	public static final String EXTRA_EVENT = "com.alvarosantisteban.berlincurator.event";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"In the onCreate() event");
		
		// Check if the GooglePlay services for the Map can be used
		int availabilityCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if(availabilityCode == ConnectionResult.SUCCESS){
			Log.i(TAG, "GooglePlayServices is available :)");
		}
		
		setContentView(R.layout.activity_event);
		
		// Get the intent with the Event
		Intent intent = getIntent();
		event = (Event)intent.getSerializableExtra(EXTRA_EVENT);
		
		// Enable the app's icon to act as home
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		} 
		
		scrollView = (CustomScrollView) findViewById(R.id.scrollViewEvent);
		eventLayout = (LinearLayout) findViewById(R.id.eventLayout);
		interestingCheck = (CheckBox)findViewById(R.id.checkbox_interesting);
		name = (TextView)findViewById(R.id.events_name);
		day = (TextView)findViewById(R.id.date);
		time = (TextView)findViewById(R.id.events_time);
		link = (TextView)findViewById(R.id.events_link);
		description = (TextView)findViewById(R.id.events_description);
		origin = (TextView)findViewById(R.id.events_origin);
		location = (TextView)findViewById(R.id.events_location);

		// Get the information of the event
		initEventsInfo();
		
		// Initializes the mapView
		initMap(savedInstanceState);
		
	}

	/**
	 * Gets the information from the event and prints it to the screen
	 */
	private void initEventsInfo() {
		// Get the date
		day.setText(event.getDay().trim());
		
		// Get the state of the check
		interestingCheck.setChecked(event.isTheEventInteresting()); 
		
		// Get the name
		//name.setMovementMethod(LinkMovementMethod.getInstance()); The problem with this, is that is makes the text not selectable
		// http://stackoverflow.com/questions/8558732/listview-textview-with-linkmovementmethod-makes-list-item-unclickable
		name.setText(Html.fromHtml(event.getName()));
		
		// Get the hour, if any
		if(!event.getHour().equals("")){
			time.setText(Html.fromHtml(event.getHour()));
		}
		
		// Get the description, if any
		if (!event.getDescription().equals("")){
			description.setText(Html.fromHtml(event.getDescription()));
			description.append("\n" +event.getThemaTag() + " | " +event.getTypeTag());
		}
		
		// Get the origin of the information
		origin.setText(Html.fromHtml("Event taken from: <a href=\"" +event.getOriginsWebsite() + "\">" +event.getEventsOrigin()  + "</a>"));
		//origin.setClickable(true);
		origin.setMovementMethod (LinkMovementMethod.getInstance());
		
		// Get the links, if any
		if (!event.getLink().equals("")){
			String[] links = event.getLinks();
			link.setText(links[0] + " \n " +links[1]);
			// Make the link clickable. The links have no html, so we make them clickable this way
			Linkify.addLinks(link, Linkify.WEB_URLS);
			link.setMovementMethod(LinkMovementMethod.getInstance());
			
		}
		
		// Get the location, if any
		if(!event.getLocation().equals("")){
			//location.setText(event.getLocation());
			//location.setMovementMethod(LinkMovementMethod.getInstance());
			location.setText(Html.fromHtml(event.getLocation()));
			//Linkify.addLinks(location, Linkify.WEB_URLS);
			//location.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}

	/**
	 * Initializes the mapView, sets the position of the event on it and adds the map to the layout.
	 * 
	 */
	private void initMap(Bundle savedInstanceState) {
		geocoder = new Geocoder(this);	
		try {
			List<Address> addressList = geocoder.getFromLocationName(event.getLocation(), 1); 
			if (addressList != null && addressList.size() > 0) {
                double lat = addressList.get(0).getLatitude();  
                double lng = addressList.get(0).getLongitude();  
                GoogleMapOptions options = new GoogleMapOptions();
        		options.camera(new CameraPosition(new LatLng(lat, lng), 15, 0, 0));  
        		mapita = new MapView(this, options);
        		mapita.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 400)); 
        		mapita.onCreate(savedInstanceState); 		
        		eventLayout.addView(mapita);
        		scrollView.setMap(mapita);
        		GoogleMap mMap = mapita.getMap();
        		if(mMap != null){
        			mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(event.getName()).snippet(event.getLocation()));
        		}
            }  
		} catch (IOException e) {
			Log.e(TAG,"Problem getting the Address for the map.");
			e.printStackTrace();
		}
	}
	

	// ----------------------------------------------------------------------------------------------
	// INTERACTING WITH ELEMENTS OF THE EVENTACTIVITY
	// ----------------------------------------------------------------------------------------------
	
	/**
	 * Adds the event to a Google Calendar of the user.
	 * 
	 * @param v the View
	 */
	public void addToMyGoogleCalendar(View v){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			Calendar cal = new GregorianCalendar(); 
			cal.setTime(new Date()); 
			cal.add(Calendar.MONTH, 2); 
			Intent intent = new Intent(Intent.ACTION_INSERT); 
			intent.setData(Events.CONTENT_URI); 
			intent.putExtra(Events.TITLE, name.getText().toString()); 
			intent.putExtra(Events.DESCRIPTION, description.getText().toString()); 
			intent.putExtra(Events.EVENT_LOCATION, location.getText().toString());
			// If we know when will the event begin 
			if (!time.getText().toString().equals("")){
				long startEvent = getTimeInMilliseconds(day.getText().toString(),time.getText().toString());
				intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startEvent); 
			}else{
				// If not, we set it to happen during the whole day
				intent.putExtra(Events.ALL_DAY, true); 
				long startEvent = getTimeInMilliseconds(day.getText().toString(),"00:00");
				intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startEvent); 
			}		
			startActivity(intent); 
		}else{
			Toast toast = Toast.makeText(getBaseContext(), "You need to have an Android with version at least 4.0 to add events to your Google Calendar", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP, 0, FirstTimeActivity.actionBarHeight);
			toast.show();
		}
	}
	
	/**
	 * Retrieves the time in milliseconds for a start time of an event on a concrete date
	 * 
	 * @param date the day when the event will take place in the format DD/MM/YYYY
	 * @param startTime the time when the event will take place in the format HH:MM
	 * @return a long with the number of milliseconds
	 */
	private long getTimeInMilliseconds(String date, String startTime) {
		int year, month, day;
		String[] dayMonthYear = date.split("/");
		day = Integer.parseInt(dayMonthYear[0]);
		month = Integer.parseInt(dayMonthYear[1]);
		year = Integer.parseInt(dayMonthYear[2]);
		
		int hour, minutes;
		String[] hourMinutes = startTime.split(":");
		hour = Integer.parseInt(hourMinutes[0]);
		minutes = Integer.parseInt(hourMinutes[1]);
		
		Calendar beginTime = Calendar.getInstance();
		beginTime.set(year, month-1, day, hour, minutes); // Month starts in 0
		return beginTime.getTimeInMillis();
	}
	
	/**
	 * Listener for the checkbox that determines if an event is interesting to a user or not.
	 * 
	 * @param view the CheckBox clicked
	 */
	public void onCheckboxClicked(View view) {
	    // Is the view now checked?
	    boolean checked = ((CheckBox) view).isChecked();
	    event.markEventAsInteresting(checked);
	    Log.d(TAG,event.getName() +"with id:" +event.getId() +" is interesting =" +event.isTheEventInteresting());
	    if (view.getId() == R.id.checkbox_interesting) {
	    	Toast toast;
			if (checked){
				toast = Toast.makeText(getBaseContext(), R.string.mark_event, Toast.LENGTH_SHORT);
			}else{
				toast = Toast.makeText(getBaseContext(), R.string.unmark_event, Toast.LENGTH_SHORT);
			}
			toast.setGravity(Gravity.TOP, 0, FirstTimeActivity.actionBarHeight);
			toast.show();
			// Get our dao
			RuntimeExceptionDao<Event, Integer> eventDao = getHelper().getEventDataDao();
			// Update the event
			eventDao.update(event);
			// Tell DateActivity that the event was updated and return it
			Intent returnIntent = new Intent();
			returnIntent.putExtra(DateActivity.EVENTS_RESULT_DATA, event);
			setResult(DateActivity.RESULT_UPDATE, returnIntent);  
		}
	}
	

	// ----------------------------------------------------------------------------------------------
	// MENU RELATED
	// ----------------------------------------------------------------------------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event, menu);
		return true;
	}
	
	/**
	 * Checks which item from the menu has been clicked
	 */
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_calendar) {
        	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				Intent i2 = new Intent(this, CalendarActivity.class);
				startActivity(i2);
        	}else{
        		/*
        		Toast toast = Toast.makeText(getBaseContext(), "You need to have an Android with version at least 3.0 to select the day", Toast.LENGTH_SHORT);
    			toast.setGravity(Gravity.TOP, 0, FirstTimeActivity.actionBarHeight);
    			toast.show();
    			*/
    			Intent i2 = new Intent(this, FakeCalendarActivity.class);
				startActivity(i2);
        	}
		} else if (item.getItemId() == android.R.id.home) {
			// app icon in action bar clicked; go to the DateActivity
            Intent intent = new Intent(this, DateActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
 
        return true;
    }

	// ----------------------------------------------------------------------------------------------
	// LIFECYCLE
	// ----------------------------------------------------------------------------------------------
	
	public void onStart() {
		super.onStart();
		Log.d(TAG, "In the onStart() event");
	}		   

	public void onRestart() {
		super.onRestart();
		Log.d(TAG, "In the onRestart() event");
	}
	    
	public void onResume() {
		super.onResume();
		if (mapita != null){
			mapita.onResume();
		}
		Log.d(TAG, "In the onResume() event");
	}
	    
	public void onPause() {
	    super.onPause();
	    if (mapita != null){
	    	mapita.onPause();
	    }
	    Log.d(TAG, "In the onPause() event");
	}
	    
	public void onStop() {
	    super.onStop();
	    Log.d(TAG, "In the onStop() event");
	}
	    
	public void onDestroy() {
	    super.onDestroy();
	    if (mapita != null){
	    	mapita.onDestroy();
	    }
	    Log.d(TAG, "In the onDestroy() event");
	}
	
	public void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		if (mapita != null){
			mapita.onSaveInstanceState(savedInstanceState);
		}
	}
	
	public void onLowMemory(){
		super.onLowMemory();
		if (mapita != null){
			mapita.onLowMemory();
		}
	}	
		
}
