package com.alvarosantisteban.berlincurator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Creates the main screen, from where the user can access the Settings, load the events of the current day or 
 * go to Calendar to select another day
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class FirstTimeActivity extends OrmLiteBaseActivity<DatabaseHelper> {

	public static final String EXTRA_HTML = "com.alvarosantisteban.berlincurator.html";
	//public static List<List<Event>> events = (ArrayList)new ArrayList <ArrayList<Event>>();
	
	String tag = "First time Activity";
	
	public static int actionBarHeight;
	
	/**
	 *  Settings
	 */
	private static final int RESULT_SETTINGS = 1;
	/**
	 *  User preferences
	 */
	SharedPreferences sharedPref;
	Context context;
	
	/**
	 * The total set of webs where the events can be extracted
	 */
	public static String[] websNames = {IHeartBerlinEventLoader.WEBSITE_NAME, 
										ArtParasitesEventLoader.WEBSITE_NAME, 
										MetalConcertsEventLoader.WEBSITE_NAME, 
										WhiteTrashEventLoader.WEBSITE_NAME, 
										KoepiEventLoader.WEBSITE_NAME, 
										GothDatumEventLoader.WEBSITE_NAME, 
										StressFaktorEventLoader.WEBSITE_NAME, 
										IndexEventLoader.WEBSITE_NAME};

   	/**
   	 * The progress bar for downloading and extracting the events
   	 */
	ProgressBar loadProgressBar;
	
	/**
	 * The button that triggers the download and extraction of events
	 */
    Button loadButton;
    
    private static Toast toast;
	
	/**
	 * Loads the elements from the resources
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("--------------- BEGIN ------------");
		// Set the context
		context = this;
		setContentView(R.layout.activity_first_time);
		loadButton = (Button) findViewById(R.id.loadButton);
		loadProgressBar = (ProgressBar)findViewById(R.id.progressLoadHtml);	
		toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		
		// Get the default shared preferences
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		// This is suppose to be used to clear the data from shared preferences
		//Editor editor = sharedPref.edit();
		//editor.clear();
		//editor.commit();
		
		// CODIGO PARA BORRAR DE LA BASE DE DATOS
		// The first one creates problems, the second one takes longer
		//context.deleteDatabase("berlincurator.db");
		/*
		try {
			TableUtils.clearTable(getConnectionSource(), Event.class);
		} catch (java.sql.SQLException e) {
			System.out.println("PETATTTSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
			e.printStackTrace();
		}
		*/		
		 
		
		// Get the sites are meant to be shown
		Set<String> setOfWebsites = sharedPref.getStringSet(SettingsFragment.KEY_PREF_MULTILIST_SITES, new HashSet<String>(Arrays.asList(context.getResources().getStringArray(R.array.default_sites_array))));
		websNames = setOfWebsites.toArray(new String[0]);
		
		// Mark that the App has been used
		Editor editor = sharedPref.edit();
		editor.putBoolean("isFirstTimeApp", false);
		editor.commit();
		
		/*
		 * To check that the websites are there
		*/
		System.out.println();
		for(int i=0;i<websNames.length;i++){
			System.out.print(websNames[i] + " / ");
		}
		System.out.println();
		
		// Get the height of the action bar
		TypedValue tv = new TypedValue();
		context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
		actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);

		// Listener for the load button
		loadButton.setOnClickListener(new OnClickListener() {
			
			/**
			 * Downloads the html from the websites and goes to the DataActivity
			 */
			public void onClick(View v) {
				
				// prepare for a progress bar dialog	
				loadButton.setEnabled(false);
				
				Toast toast = Toast.makeText(getBaseContext(), "Downloading the events.\nIt might take a few seconds, be patient ;)", Toast.LENGTH_LONG);
		    	toast.setGravity(Gravity.TOP, 0, FirstTimeActivity.actionBarHeight);
		    	toast.show();
				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			    // Check if is possible to establish a connection
			    if (networkInfo != null && networkInfo.isConnected()) {
					DownloadWebpageTask download = new DownloadWebpageTask();
					// Execute the asyncronous task of downloading the websites
					download.execute(websNames);
			    } else {
			    	// Inform the user that there is no network connection available
			    	toast = Toast.makeText(getBaseContext(), "No network connection available.", Toast.LENGTH_LONG);
			    	toast.setGravity(Gravity.TOP, 0, FirstTimeActivity.actionBarHeight);
			    	toast.show();
			        System.out.println("No network connection available.");
			    }
			    
			}
		});	
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
	}

	
	/**
	 * Inflates the menu from the XML
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.first_time, menu);
		return true;
	}
	
	/**
	 * Checks which item from the menu has been clicked
	 */
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
			Intent i = new Intent(this, SettingsActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
		}
 
        return true;
    }

	private void displayToast(final String message) {
	    //toast.cancel();
	    toast.setText(message); 
	    toast.setGravity(Gravity.TOP, 0, FirstTimeActivity.actionBarHeight);
	    toast.show();
	}
    
    /** 
     * Uses AsyncTask to create a task away from the main UI thread. 
     * This task takes the creates several HttpUrlConnection to download the html from different websites. 
     * Afterwards, the several lists of Events are created and the execution goes to the Date Activity.
    */
	public class DownloadWebpageTask extends AsyncTask<String, String, Integer> {
		
		/**
		 * Makes the progressBar visible
		 */
		protected void onPreExecute(){
	    	System.out.println("onPreExecute");
	    	loadProgressBar.setVisibility(View.VISIBLE);
	    	lockScreenOrientation();
	    	// Inform the user
	    	publishProgress("Start", "");
		}
		
		/**
		 * Downloads the htmls and creates the lists of Events. 
		 * Detects any possible problem during the download of the website or the extraction of events.
		 * @return 
		 * 
		 */
		protected Integer doInBackground(String... urls) { 
			int numOfNewEvents = 0;
			// Load the events from the selected websites
			for (int i=0; i<urls.length; i++){
				List<Event> eventsFromAWebsite = null;
				if (urls[i].equals("I Heart Berlin")){
					System.out.println("Ihearberlin dentro");
					eventsFromAWebsite = EventLoaderFactory.newIHeartBerlinEventLoader().load(context);
				}else if(urls[i].equals("Berlin Art Parasites")){
					System.out.println("artParasites dentro");
					eventsFromAWebsite = EventLoaderFactory.newArtParasitesEventLoader().load(context);
				}else if(urls[i].equals("Metal Concerts")){
					System.out.println("metalConcerts dentro");
					eventsFromAWebsite = EventLoaderFactory.newMetalConcertsEventLoader().load(context);
				}else if(urls[i].equals("White Trash")){
					System.out.println("whitetrash dentro");
					eventsFromAWebsite = EventLoaderFactory.newWhiteTrashEventLoader().load(context);
				}else if(urls[i].equals("Köpi's events")){
					System.out.println("koepi dentro");
					eventsFromAWebsite = EventLoaderFactory.newKoepiEventLoader().load(context);
				}else if(urls[i].equals("Goth Datum")){
					System.out.println("goth dentro");
					eventsFromAWebsite = EventLoaderFactory.newGothDatumEventLoader().load(context);
				}else if(urls[i].equals("Stress Faktor")){
					System.out.println("Stresssssss faktor dentro");
					eventsFromAWebsite = EventLoaderFactory.newStressFaktorEventLoader().load(context);
				}else if(urls[i].equals("Index")){
					System.out.println("Index dentro");
					eventsFromAWebsite = EventLoaderFactory.newIndexEventLoader().load(context);
				}else{
					return null;
				}
				// If there was a problem loading the events we tell the user
				if (eventsFromAWebsite == null){
					// Distinguish the situation where the event is null because the Berlin Art Parasites website is not checked
					if(!(urls[i].equals("Berlin Art Parasites") && !ArtParasitesEventLoader.isBerlinWeekend())){
						System.out.println("Event is null");
						publishProgress("Exception", urls[i]);	
					}
				}else{
					// Add the events from this website to the DB
					for (int j = 0; j < eventsFromAWebsite.size(); j++) {
						if(addEventToDB(eventsFromAWebsite.get(j))){
							numOfNewEvents++;
						}
					}
				}
			}
			// Inform the user
			publishProgress("Finish",Integer.valueOf(numOfNewEvents).toString());
			return numOfNewEvents;
		}  
		
		/**
		 * Adds an event to the DB if it does not already exist.
		 * 
		 * @param event The event to be added
		 * @return true if the event was added, false otherwise.
		 */
		private boolean addEventToDB(Event event) {
			// Get our dao
			RuntimeExceptionDao<Event, Integer> eventDao = getHelper().getEventDataDao();
			// Check if the event already exists in the DB
			if(!isEventInDB(eventDao, event)){
				// Store the event in the database
				eventDao.create(event);
				return true;
			}
			return false;
		}

		/**
		 * Checks if an event is already in the DB. To determinate it, checks the name, day and description of an event.
		 * 
		 * @param eventDao Our DAO for the Events table
		 * @param event The event to be checked its presence in the DB
		 * @return true if the event is already in the DB
		 */
		private boolean isEventInDB(RuntimeExceptionDao<Event, Integer> eventDao, Event event) {
			Map<String, Object> fieldValues = new HashMap<String,Object>();
			fieldValues.put("name", event.getName());
			fieldValues.put("day", event.getDay());
			fieldValues.put("description", event.getDescription());
			List<Event> foundEvents = eventDao.queryForFieldValuesArgs(fieldValues);
			/*
			 * for (int i=0; i<foundEvents.size();i++){
				System.out.println(foundEvents.get(i).getName());
			}
			*/
			if(foundEvents.size() >= 1){
				System.out.println("El evento ya existe, no se añade.");
				
				return true;
			}
			return false;
		}

		/**
		 * Informs the user of the state of the process of events download.
		 */
		protected void onProgressUpdate(String... progress) {
    		System.out.println("Estoy en onProgressUpdate:"+progress[0]);
    		if (progress[0].equals("Start")){
    			displayToast(getString(R.string.searching));
    		}else if (progress[0].equals("Exception")){
    			displayToast("There were problems downloading the content from: " +progress[1] +" It's events won't be displayed.");
    		}else if (progress[0].equals("Finish")){
    			if (Integer.parseInt(progress[1]) > 0){
    				displayToast("Added " +progress[1] +" new events");
    			}else{
    				displayToast(getString(R.string.no_new_events));
    			}
    		}
    		//loadProgressBar.setProgress(progress[0].intValue());
		}
		
		/**
		* Goes to the Date Activity and hides the progressBar.
        */
		protected void onPostExecute(Integer result) {
			System.out.println("onPostExecute------------>");
			loadProgressBar.setVisibility(View.GONE);
			unlockScreenOrientation();
			// Reload the Date Activity if there are new events
			if (result > 0){
				Intent intent = new Intent(context, DateActivity.class);
				startActivity(intent);
			}
		}
		
		private void lockScreenOrientation() {
		    int currentOrientation = getResources().getConfiguration().orientation;
		    if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
		        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		    } else {
		        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		    }
		}
		 
		private void unlockScreenOrientation() {
		    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}
}