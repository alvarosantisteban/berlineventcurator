package com.alvarosantisteban.pathos;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.alvarosantisteban.pathos.model.CustomScrollView;
import com.alvarosantisteban.pathos.model.Event;
import com.alvarosantisteban.pathos.utils.Constants;
import com.alvarosantisteban.pathos.utils.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.*;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Displays all the information of an event: Day, name, hour, description, origin, tags, links and map.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class EventActivity extends OrmLiteBaseActivity<DatabaseHelper> {

	/**
     * Used for logging purposes
     */
    private static final String TAG = "EventActivity";
	private static final int MAP_HEIGHT = 400;

	CustomScrollView scrollView;
	LinearLayout eventLayout;
	TextView name;
	TextView day;
	TextView time;
	TextView link;
	TextView description;
	TextView origin;
	TextView location;
	TextView tags;
	CheckBox interestingCheck;

	MapView mapita;

	// The listener for the clicks on the window of the marker
	OnInfoWindowClickListener infoWindowListener = new OnInfoWindowClickListener() {

		@Override
		public void onInfoWindowClick(Marker marker) {
			marker.hideInfoWindow();
		}
	};
	
	/**
	 * The event being displayed
	 */
	Event event;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG,"In the onCreate() event");
		
		setContentView(R.layout.activity_event);
		
		// Get the intent with the Event
		Intent intent = getIntent();
		event = intent.getParcelableExtra(Constants.EXTRA_EVENT);
		
		// Enable the app's icon to act as home
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			if (actionBar != null) {
				actionBar.setDisplayHomeAsUpEnabled(true);
			}
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
		tags = (TextView)findViewById(R.id.events_tags);
		location = (TextView)findViewById(R.id.events_location);

		// Get the information of the event
		initEventsInfo();
		
		// Check if the GooglePlay services for the Map can be used
		int availabilityCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if(availabilityCode == ConnectionResult.SUCCESS){
			MapsInitializer.initialize(getApplicationContext());

			// Initializes the mapView
			initMap(savedInstanceState);
		}
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
		}
		
		// Get the origin of the information
		origin.setText(Html.fromHtml("Event taken from: <a href=\"" +event.getOriginsWebsite() + "\">" +event.getEventsOrigin()  + "</a>"));
		//origin.setClickable(true);
		origin.setMovementMethod (LinkMovementMethod.getInstance());
		
		// Get the tags
		tags.setText(event.getThemaTag() + " | " +event.getTypeTag());
		
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
	 * Initializes the mapView and throws a GeoCodeAsyncTask to set the position of the event on it.
	 * 
	 */
	private void initMap(Bundle savedInstanceState) {
		// Set the mapView
		mapita = new MapView(this);
		mapita.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, MAP_HEIGHT));
		mapita.onCreate(savedInstanceState);
		eventLayout.addView(mapita);
		scrollView.setMap(mapita);

		// Throw the AsyncTask to locate the event
		new GeoCodeAsyncTask(this).execute();
	}

	/////////////////////////////////////////////////////////////////////////
	// INTERACTING WITH ELEMENTS OF THE EVENT ACTIVITY
	/////////////////////////////////////////////////////////////////////////
	
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
	    Log.d(TAG,event.getName() +" with id:" +event.getId() +" is interesting =" +event.isTheEventInteresting());
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

	/////////////////////////////////////////////////////////////////////////
	// ASYNC TASK TO GEOCODE
	/////////////////////////////////////////////////////////////////////////

	private class GeoCodeAsyncTask extends AsyncTask <Void, Void, Address>{

		private Context context;

		public GeoCodeAsyncTask(Context theContext){
			context = theContext;
		}

		@Override
		protected Address doInBackground(Void... params) {
			Geocoder geocoder = new Geocoder(context);
			try {
				List<Address> addressList = geocoder.getFromLocationName(event.getLocation(), 1);
				if (addressList != null && addressList.size() > 0) {
					return addressList.get(0);
				}
			} catch (IOException e) {
				Log.e(TAG, "Problem getting the address for the map while geocoding.");
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Address theAddress){
			if (theAddress != null) {
				GoogleMap mMap = mapita.getMap();
				if (mMap != null) {
					double lat = theAddress.getLatitude();
					double lng = theAddress.getLongitude();

					// Add the marker
					mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
							.title(event.getName())
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker)));
					mMap.setOnInfoWindowClickListener(infoWindowListener);

					// Update the camera position
					CameraPosition cameraPosition = new CameraPosition(new LatLng(lat, lng), 15, 0, 0);
					CameraUpdate update = CameraUpdateFactory.newCameraPosition(cameraPosition);
					mMap.moveCamera(update);
				}
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// MENU RELATED
	/////////////////////////////////////////////////////////////////////////

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
    			Intent i2 = new Intent(this, FakeCalendarActivity.class);
				startActivity(i2);
        	}
		} else if (item.getItemId() == android.R.id.home) {
			// app icon in action bar clicked; go to the DateActivity
			finish();
		}
 
        return true;
    }

	/////////////////////////////////////////////////////////////////////////
	// LIFECYCLE
	/////////////////////////////////////////////////////////////////////////

	@Override
	public void onStart() {
		super.onStart();
		Log.v(TAG, "In the onStart() event");
	}		   

	@Override
	public void onRestart() {
		super.onRestart();
		Log.v(TAG, "In the onRestart() event");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mapita != null){
			mapita.onResume();
		}
		Log.v(TAG, "In the onResume() event");
	}

	@Override
	public void onPause() {
	    super.onPause();
	    if (mapita != null){
	    	mapita.onPause();
	    }
	    Log.v(TAG, "In the onPause() event");
	}

	@Override
	public void onStop() {
	    super.onStop();
	    Log.v(TAG, "In the onStop() event");
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    if (mapita != null){
	    	mapita.onDestroy();
	    }
	    Log.v(TAG, "In the onDestroy() event");
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		if (mapita != null){
			mapita.onSaveInstanceState(savedInstanceState);
		}
	}

	@Override
	public void onLowMemory(){
		super.onLowMemory();
		if (mapita != null){
			mapita.onLowMemory();
		}
	}
}
