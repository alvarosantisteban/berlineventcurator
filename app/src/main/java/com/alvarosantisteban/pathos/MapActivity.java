package com.alvarosantisteban.pathos;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.alvarosantisteban.pathos.utils.Constants;
import com.alvarosantisteban.pathos.utils.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
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
 * the user location is also shown, if not, it shows a popup dialog.
 * 
 * @author Alvaro Santisteban 2014 - alvarosantisteban@gmail.com
 *
 */
public class MapActivity extends OrmLiteBaseActivity<DatabaseHelper>  implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
	
	private static final String TAG = "MapActivity";

    private static final int REQUEST_CHECK_SETTINGS = 1;

	private static final String EXTRA_DATE = "date";
	public static final String EXTRA_EVENT = "com.alvarosantisteban.pathos.event";
	
	private final String BUNDLE_EVENTS_LIST = "eventsList";
	
	static final LatLng BERLIN = new LatLng(52.49333, 13.36446);

    // The map
	GoogleMap map;
	
	// Used to locate the user
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
	Marker userMarker;

    // The callback for the management of the user settings regarding location
    private ResultCallback<LocationSettingsResult> mResultCallbackFromSettings = new ResultCallback<LocationSettingsResult>() {
        @Override
        public void onResult(LocationSettingsResult result) {
            final Status status = result.getStatus();
            //final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(
                                MapActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.e(TAG, "Settings change unavailable. We have no way to fix the settings so we won't show the dialog.");
                    break;
            }
        }
    };

    // The click listener of the info window of the markers of the events.
    OnInfoWindowClickListener InfoWindowListener = new OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            for (Event event : eventsList) {
                if (event.getDescription().equals(marker.getSnippet()) && event.getName().equals(marker.getTitle())) {
                    Intent intent = new Intent(context, EventActivity.class);
                    intent.putExtra(EXTRA_EVENT, event);
                    startActivity(intent);
                }
            }
        }
    };

    // Fields related to the datum
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
	Calendar currentDay = Calendar.getInstance();
	private String today = dateFormat.format(currentDay.getTime());
	String chosenDate;
	
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
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        // Get the chosen date from the calendar
		Intent intent = getIntent();
		chosenDate = intent.getStringExtra(EXTRA_DATE);
		if (chosenDate == null){
			// The user did not select anything, the default date is today
			sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			chosenDate = sharedPref.getString(Constants.CHOSEN_DATE, today);
		}

        // Set the map
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(BERLIN, 12));
	    map.setOnInfoWindowClickListener(InfoWindowListener);
		
	    // Load the events and set the markers
		new LoadEventsAsyncTask().execute((String)null);

        // Build google API Client
        buildGoogleApiClient();

        // Create the location request
        createLocationRequest();

        // Check the location settings of the user and create the callback to react to the different possibilities
        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, locationSettingsRequestBuilder.build());
        result.setResultCallback(mResultCallbackFromSettings);
    }
	    
	///////////////////////////////////////////////////////////////////////////
	// ASYNC TASKS
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
			// Get our dao
			RuntimeExceptionDao<Event, Integer> eventDao = getHelper().getEventDataDao();
			List<Event> events = null;
			try {
				Map<String, Object> fieldValues = new HashMap<String,Object>();
				fieldValues.put("day", chosenDate);
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
					Log.v(TAG, "Event: "+event.getName()+" added as a marker");
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

    /**
     * Get the last location and start a location update
     */
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

    /**
     * Creates the mGoogleApiClient for the LocationServices API
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Creates the mLocationRequest
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    /**
     * Updates the user location and the UI.
     *
     * @param newLocation the new user Location
     */
	@Override
	public void onLocationChanged(Location newLocation) {
        mLastLocation = newLocation;
        updateUI();

        // Do not ask for more updates
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
	}

    /**
     * Starts the user location updates
     */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Stops the user location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Adds a marker for the user in their last known location
     */
    private void updateUI() {
        LatLng ltLg = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        userMarker = map.addMarker(new MarkerOptions().position(ltLg)
                .title(getString(R.string.map_user_marker_title))
                .snippet(getString(R.string.map_user_marker_snippet)));
    }

    ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    ///////////////////////////////////////////////////////////////////////////

	@Override
	protected void onPause() {
		Log.v(TAG, "onPause");
		super.onPause();

        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
	}

	@Override
	protected void onResume() {
		Log.v(TAG, "onResume");
		super.onResume();

        if (mGoogleApiClient.isConnected() && userMarker == null) {
            startLocationUpdates();
        }
	}

    @Override
    protected void onStart() {
        super.onStart();

        // Connect the GoogleAPIClient
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnect the GoogleAPIClient
        mGoogleApiClient.disconnect();

        super.onStop();
    }
	
	@Override
	public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
		Log.v(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putParcelableArrayList(BUNDLE_EVENTS_LIST, (ArrayList<? extends Parcelable>) eventsList);
	}
	
	@Override
	public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		Log.v(TAG, "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
		eventsList = savedInstanceState.getParcelableArrayList(BUNDLE_EVENTS_LIST);
	}

    /**
     * Used to check the result of the check of the user location settings
     *
     * @param requestCode code of the request made
     * @param resultCode code of the result of that request
     * @param intent intent with further information
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(intent);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        if (mGoogleApiClient.isConnected() && userMarker == null) {
                            startLocationUpdates();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // MENU RELATED
    ///////////////////////////////////////////////////////////////////////////
	
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
