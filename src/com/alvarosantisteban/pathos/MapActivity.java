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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;

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

public class MapActivity extends OrmLiteBaseActivity<DatabaseHelper>{
	
	private static final String TAG = "MapActivity";
	
	private final String CHOOSEN_DATE = "choosenDate";
	private final String EXTRA_DATE = "date";
	public static final String EXTRA_EVENT = "com.alvarosantisteban.pathos.event";
	
	static final LatLng BERLIN = new LatLng(52.49333, 13.36446);
	
	MapView generalMap;
	GoogleMap map;
	
	
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
		Marker berlin = map.addMarker(new MarkerOptions().position(BERLIN)
		        .title("Berlin")
		        .snippet("Berlin is cool")
		        .icon(BitmapDescriptorFactory
		            .fromResource(R.drawable.ic_launcher_pathos)));

		// Move the camera instantly to Berlin with a zoom of 15.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(BERLIN, 15));
		
		eventsList = loadAllEvents();
		addMarkerForEvents(eventsList);
		

		map.setOnInfoWindowClickListener(InfoWindowListener);

		// Zoom in, animating the camera.
		//map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
	}
	
	
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 	
		}	
	}

	OnInfoWindowClickListener InfoWindowListener = new OnInfoWindowClickListener(){
		@Override
		public void onInfoWindowClick(Marker marker) {
			for (Event event : eventsList){
				if(event.getName().equals(marker.getTitle())){
					Intent intent = new Intent(context, EventActivity.class);
					intent.putExtra(EXTRA_EVENT, event);
					startActivity(intent);
				}
			}
	    }};

	/**
	 * Loads the events from the DB that match the choosenDate and the set of tags for the kind of organization passed as parameters
	 * 
	 * @param kindOfOrganization the kind of organization (By Type, Thema, Origin)
	 * @param setOfTags the set of tags that the event has to match in order to be extracted from the DB
	 */
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
	}
	
	private List<Event> loadAllEvents(){ 
		Log.d(TAG,"LOAD ALL EVENTS");
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}
}
