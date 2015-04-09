package com.alvarosantisteban.pathos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alvarosantisteban.pathos.loader.EventLoaderFactory;
import com.alvarosantisteban.pathos.utils.Constants;
import com.alvarosantisteban.pathos.utils.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Asyncronous Task used to download a set of html from different websites. 
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class DownloadWebpageAsyncTask extends AsyncTask<String, String, Integer> {

    private static final String TAG = "DownloadWebpageAsyncTask";
	
	private Context context;
	private ProgressBar loadProgressBar;
	private Toast toast;
	private String choosenDate;
	private int numOfEventsInDate = 0;
	
	/**
	* The Database Helper that helps dealing with the db easily
	*/
	private DatabaseHelper databaseHelper = null;
	
	private String initialMessage;

	public DownloadWebpageAsyncTask(Context context, ProgressBar theLoadProgressBar, String theChoosenDate, String theInitialMessage) {
	    this.context = context;
	    this.loadProgressBar = theLoadProgressBar;
	    this.choosenDate = theChoosenDate;
	    this.initialMessage = theInitialMessage;
	    toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
	    
	}
	
	/**
	 * Makes the progressBar visible
	 */
	protected void onPreExecute(){
    	Log.d(TAG, "onPreExecute");
    	if(loadProgressBar != null){
    		loadProgressBar.setVisibility(View.VISIBLE);
    	}
    	// Inform the user
    	publishProgress("Start", "");
    	lockScreenOrientation();
	}
	
	/**
	 * Downloads the htmls and creates the lists of Events. 
	 * Detects any possible problem during the download of the website or the extraction of events.
	 * @return the number of new events
	 * 
	 */
	protected Integer doInBackground(String... urls) { 
		int numOfNewEvents = 0;
		// Load the events from the selected websites
		for (int i=0; i<urls.length; i++){
			List<Event> eventsFromAWebsite = null;
			if (urls[i].equals("I Heart Berlin")){
				Log.d(TAG, "Inside Ihearberlin");
				eventsFromAWebsite = EventLoaderFactory.newIHeartBerlinEventLoader().load(context);
			}else if(urls[i].equals("Metal Concerts")){
				Log.d(TAG, "Inside metalConcerts");
				eventsFromAWebsite = EventLoaderFactory.newMetalConcertsEventLoader().load(context);
			}else if(urls[i].equals("Goth Datum")){
				Log.d(TAG, "Inside Goth");
				eventsFromAWebsite = EventLoaderFactory.newGothDatumEventLoader().load(context);
			}else if(urls[i].equals("Stress Faktor")){
				Log.d(TAG, "Inside Stresssssss faktor");
				eventsFromAWebsite = EventLoaderFactory.newStressFaktorEventLoader().load(context);
			}else if(urls[i].equals("Index")){
				Log.d(TAG, "Inside Index");
				eventsFromAWebsite = EventLoaderFactory.newIndexEventLoader().load(context);
			}else{
				return null;
			}
			// If there was a problem loading the events we tell the user
			if (eventsFromAWebsite == null){
				publishProgress("Exception", urls[i]);
			}else{
				// Add the events from this website to the DB
				for (int j = 0; j < eventsFromAWebsite.size(); j++) {
					if(addEventToDB(eventsFromAWebsite.get(j))){
						numOfNewEvents++;
						if(eventsFromAWebsite.get(j).getDay().equals(choosenDate)){
							numOfEventsInDate++;
						}
					}
				}
			}
		}
		// Inform the user that the download finished
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
		
		if(foundEvents.size() >= 1){
			//Log.v(TAG,"El evento ya existe, no se a�ade.");
			return true;
		}
		return false;
	}

	/**
	 * Informs the user of the state of the process of events download.
	 */
	protected void onProgressUpdate(String... progress) {
		Log.d(TAG,"Estoy en onProgressUpdate:"+progress[0]);
		if (progress[0].equals("Start")){
			displayToast(initialMessage);
		}else if (progress[0].equals("Exception")){
			displayToast("There were problems downloading the content from: " +progress[1] +" It's events won't be displayed.");
		}else if (progress[0].equals("Finish")){
			if (Integer.parseInt(progress[1]) > 0){
				displayToast("Added " +progress[1] +" new events, " +numOfEventsInDate +" for this day.");
			}else{
				displayToast(context.getString(R.string.no_new_events));
			}
		}
	}
	
	/**
	* Goes to the Date Activity and hides the progressBar.
    */
	protected void onPostExecute(Integer result) {
		Log.d(TAG,"onPostExecute------------>");
		if(loadProgressBar != null){
			loadProgressBar.setVisibility(View.GONE);
		}
		
		// Reload the Date Activity if there are new events
		unlockScreenOrientation();
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
		
		// If there are new events for the current date, reload the DateActivity
		if(numOfEventsInDate>0){
			Intent intent = new Intent(context, DateActivity.class);
			intent.putExtra(Constants.EXTRA_DATE, choosenDate);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			((Activity) context).startActivity(intent);
		}
	}
	
	/**
	 * Looks the screen. 
	 * Used to avoid having problems during the download.
	 */
	private void lockScreenOrientation() {
	    int currentOrientation = context.getResources().getConfiguration().orientation;
	    if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
	    	((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    } else {
	    	((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    }
	}
	
	/**
	 * Unlocks the screen.
	 */
	private void unlockScreenOrientation() {
		((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}
	
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
	 * Return the current DB Helper or creates one if there is none.
	 * @return the database helper
	 */
	private DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		}
		return databaseHelper;
	}
}
