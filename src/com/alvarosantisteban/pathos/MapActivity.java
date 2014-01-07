package com.alvarosantisteban.pathos;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.alvarosantisteban.pathos.utils.DatabaseHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;

/**
 * Displays a map of Berlin with a set of markers representing all the events for the selected day. If gps is activated, 
 * the user location is also shown.  
 * 
 * @author Alvaro Santisteban 2014 - alvarosantisteban@gmail.com
 *
 */
public class MapActivity extends OrmLiteBaseActivity<DatabaseHelper>  implements LocationListener{
	
	private static final String TAG = "MapActivity";
	
	private final String CHOOSEN_DATE = "choosenDate";
	private final String EXTRA_DATE = "date";
	public static final String EXTRA_EVENT = "com.alvarosantisteban.pathos.event";
	
	static final LatLng BERLIN = new LatLng(52.49333, 13.36446);
	
	MapView generalMap;
	GoogleMap map;
	
	// Used to get the GPS
	boolean gpsEnabled;
	private LocationManager locationManager;
	private String provider;
	Marker userMarker;
	
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
	Calendar currentDay = Calendar.getInstance();
	private String today = dateFormat.format(currentDay.getTime());
	String choosenDate;
	
	SharedPreferences sharedPref;
	Context context;
	
	List<Event> eventsList = new ArrayList<Event>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		context = this;
		
		// Enable home button if the device has an action bar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
		Intent intent = getIntent();
		// Get the choosen date from the calendar
		choosenDate = intent.getStringExtra(EXTRA_DATE);
		if (choosenDate == null){	
			// The user did not select anything, the default date is today
			sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			choosenDate = sharedPref.getString(CHOOSEN_DATE, today);
		}
		
		//generalMap = (MapView)findViewById(R.id.map);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(BERLIN, 15));
	    map.setOnInfoWindowClickListener(InfoWindowListener);
		
	    // Load the events and set the markers
		new LoadEventsAsyncTask().execute((String)null);
		
		// Get the location manager
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    
	    gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(gpsEnabled){
			Log.d(TAG, "gpsEnabled");
			// Define the criteria how to select the location provider -> use default
		    Criteria criteria = new Criteria();
		    provider = locationManager.getBestProvider(criteria, false);
		    
		    //Location location = locationManager.getLastKnownLocation(provider);
		    // Initialize the location fields
		    locationManager.requestLocationUpdates(provider, 400, 1, this);
		}
	}
	
	OnInfoWindowClickListener InfoWindowListener = new OnInfoWindowClickListener(){
		@Override
		public void onInfoWindowClick(Marker marker) {
			for (Event event : eventsList){
				if(event.getDescription().equals(marker.getSnippet()) && event.getName().equals(marker.getTitle())){
					Intent intent = new Intent(context, EventActivity.class);
					intent.putExtra(EXTRA_EVENT, event);
					startActivity(intent);
				}
			}
	    }};

	/*
	private void addMarkerForEvents(List<Event> eventsList) {
		for(int i=0; i<eventsList.size(); i++){
			Event event = eventsList.get(i);
			Geocoder geocoder = new Geocoder(this);
			List<Address> addressList;
			try {
				addressList = geocoder.getFromLocationName(event.getLocation(), 1);
				if (addressList != null && addressList.size() > 0) {
					Log.d(TAG, "event "+event.getName()+" added as a marker");
	                double lat = addressList.get(0).getLatitude();  
	                double lng = addressList.get(0).getLongitude();  
	                LatLng position = new LatLng(lat, lng);
	    			Marker berlin = map.addMarker(new MarkerOptions().position(position)
	    			        .title(event.getName())
	    			        .snippet(event.getDescription())
	    			        .icon(BitmapDescriptorFactory
	    			            .fromResource(R.drawable.map_marker)));

	    			berlin.showInfoWindow();
				}
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			} 	
		}	
	}*/



	/**
	 * Loads the events from the DB that match the choosenDate and the set of tags for the kind of organization passed as parameters
	 * 
	 * @param kindOfOrganization the kind of organization (By Type, Thema, Origin)
	 * @param setOfTags the set of tags that the event has to match in order to be extracted from the DB
	 */
	    /*
	private List<Event> loadEvents(String kindOfOrganization, String[] setOfTags){ 
		Log.d(TAG,"LOAD EVENTS");
		// Get our dao
		RuntimeExceptionDao<Event, Integer> eventDao = getHelper().getEventDataDao();
		for (int i=0; i<setOfTags.length; i++){
			Log.d(TAG,setOfTags[i]);
			List<Event> events = null;
			try {
				Map<String, Object> fieldValues = new HashMap<String,Object>();
				fieldValues.put(kindOfOrganization, setOfTags[i]);
				fieldValues.put("day", choosenDate);
				events = eventDao.queryForFieldValuesArgs(fieldValues);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG,"DB exception while retrieving the events from the website " +setOfTags[i]);
			}
			return events;
		}
		return null;
	}*/
	    
	    ///////////////////////////////////////////////////////////////////////////
		// ASYNCTASKS
		///////////////////////////////////////////////////////////////////////////
	/**
	 * AsyncTask that loads all events from the database
	 * 
	 * @author  Alvaro Santisteban 2014 - alvarosantisteban@gmail.com
	 *
	 */
	public class LoadEventsAsyncTask extends AsyncTask<String, Void, List<Event>> {
		
		private final static String TAG = "LoadEventsAsyncTask";

		@Override
		protected List<Event> doInBackground(String... params) {
			Log.d(TAG,"doInBackground");
			// Get our dao
			RuntimeExceptionDao<Event, Integer> eventDao = getHelper().getEventDataDao();
			List<Event> events = null;
			try {
				Map<String, Object> fieldValues = new HashMap<String,Object>();
				fieldValues.put("day", choosenDate);
				events = eventDao.queryForFieldValuesArgs(fieldValues);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG,"DB exception while retrieving all events");
			}
			return events;
		}
		
		@Override
		public void onPostExecute(List<Event> theEvents){
			eventsList = theEvents;		
			// Add the markers for all events
			for (int i=0; i<eventsList.size(); i++){
				new AddMarkersAsyncTask().execute(eventsList.get(i));
			}
		}
	}

	/**
	 * AsyncTask that adds a marker in the map for each event with a known location
	 * 
	 * @author  Alvaro Santisteban 2014 - alvarosantisteban@gmail.com
	 *
	 */
	public class AddMarkersAsyncTask extends AsyncTask<Event, Void, MarkerOptions> {
		
		private final static String TAG = "AddMarkersAsyncTask";

		@Override
		protected MarkerOptions doInBackground(Event... params) {
			Event event = params[0];
			Geocoder geocoder = new Geocoder(context);
			List<Address> addressList;
			try {
				addressList = geocoder.getFromLocationName(event.getLocation(), 1);
				if (addressList != null && addressList.size() > 0) {
					Log.d(TAG, "event "+event.getName()+" added as a marker");
	                double lat = addressList.get(0).getLatitude();  
	                double lng = addressList.get(0).getLongitude();  
	                LatLng position = new LatLng(lat, lng);
	                return new MarkerOptions().position(position)
	    			        .title(event.getName())
	    			        .snippet(event.getDescription())
	    			        .icon(BitmapDescriptorFactory
	    			            .fromResource(R.drawable.map_marker));
				}
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			} 
			return null;
		}
			
		@Override
		public void onPostExecute(MarkerOptions theMarkerOptions){
			if(theMarkerOptions != null){
				Marker berlin = map.addMarker(theMarkerOptions);
				berlin.showInfoWindow();
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// GPS
	///////////////////////////////////////////////////////////////////////////

	@Override
	public void onLocationChanged(Location newLocation) {
		Log.w(TAG, "onLocationChanged");
		LatLng ltlg = new LatLng(newLocation.getLatitude(),newLocation.getLongitude());
	    userMarker = map.addMarker(new MarkerOptions().position(ltlg)
		        .title("You")
		        .snippet("Here you are")
		        .icon(BitmapDescriptorFactory
		            .fromResource(R.drawable.ic_launcher_pathos)));
	    //map.moveCamera(CameraUpdateFactory.newLatLngZoom(ltlg, 15));
	    map.animateCamera(CameraUpdateFactory.newLatLngZoom(ltlg, 15), 1500, null);
	    if(gpsEnabled){
	    	locationManager.removeUpdates(this);
	    }
	}


	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}
	
	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		  super.onPause();
		  if(gpsEnabled){
			  locationManager.removeUpdates(this);
		  }
	  }
	  
	  /* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
		if(userMarker == null && gpsEnabled){
			locationManager.requestLocationUpdates(provider, 400, 1, this);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}
	
	
	/**
     * Check which item from the menu has been clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == android.R.id.home){
    		finish();
        }
        return true;
    }
}
