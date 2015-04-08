package com.alvarosantisteban.pathos;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.*;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.alvarosantisteban.pathos.utils.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.*;
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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Displays a map of Berlin with a set of markers representing all the events for the selected day. If gps is activated, 
 * the user location is also shown.  
 * 
 * @author Alvaro Santisteban 2014 - alvarosantisteban@gmail.com
 *
 */
public class MapActivity extends OrmLiteBaseActivity<DatabaseHelper>  implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
	
	private static final String TAG = "MapActivity";
	
	private final String CHOOSEN_DATE = "choosenDate";
	private final String EXTRA_DATE = "date";
	public static final String EXTRA_EVENT = "com.alvarosantisteban.pathos.event";
	
	private final String BUNDLE_EVENTS_LIST = "eventsList";
	
	static final LatLng BERLIN = new LatLng(52.49333, 13.36446);
	
	MapView generalMap;
	GoogleMap map;
	
	// Used to get the GPS
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
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
		Log.d(TAG, "onCreate");
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

        // Create the location request
        createLocationRequest();

        // Build google API Client
        buildGoogleApiClient();
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
	    }
    };
	    
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
	// LOCATION
	///////////////////////////////////////////////////////////////////////////

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            updateUI();
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended Google API: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed Google API: " +connectionResult.toString());
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

	@Override
	public void onLocationChanged(Location newLocation) {
        mLastLocation = newLocation;
        updateUI();

        // Do not ask for more updates
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
	}

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private void updateUI() {
        LatLng ltlg = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        userMarker = map.addMarker(new MarkerOptions().position(ltlg)
                .title(getString(R.string.map_user_marker_title))
                .snippet(getString(R.string.map_user_marker_snippet))
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_launcher_pathos)));
    }
	
	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();

        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
        /*
		if(gpsEnabled){
			locationManager.removeUpdates(this);
		}
		*/
	}
	  
	  /* Request updates at startup */
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();

        if (mGoogleApiClient.isConnected() && userMarker == null) {
            startLocationUpdates();
        }
        /*
		if(userMarker == null && gpsEnabled){
			locationManager.requestLocationUpdates(provider, 400, 1, this);
		}
		*/
	}

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putParcelableArrayList(BUNDLE_EVENTS_LIST, (ArrayList<? extends Parcelable>) eventsList);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
		eventsList = savedInstanceState.getParcelableArrayList(BUNDLE_EVENTS_LIST);
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
