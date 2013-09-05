package com.alvarosantisteban.berlincurator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.alvarosantisteban.berlincurator.loader.EventLoaderFactory;
import com.alvarosantisteban.berlincurator.utils.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;

public class DownloadWebpageAsyncTask extends AsyncTask<String, String, Integer> {
	
	private Context context;
	private ProgressBar loadProgressBar;
	private Toast toast;
	private String choosenDate;
	private int numOfEventsInDate = 0;
	
	/**
	* The Database Helper that helps dealing with the db easily
	*/
	private DatabaseHelper databaseHelper = null;
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "DownloadWebpageAsyncTask";
	private static final String EXTRA_DATE = "date";

	public DownloadWebpageAsyncTask(Context context, ProgressBar theLoadProgressBar, String theChoosenDate) {
	    this.context = context;
	    this.loadProgressBar = theLoadProgressBar;
	    this.choosenDate = theChoosenDate;
	    toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
	}
	
	/**
	 * Makes the progressBar visible
	 */
	protected void onPreExecute(){
    	Log.d(TAG,"onPreExecute");
    	loadProgressBar.setVisibility(View.VISIBLE);
    	// Inform the user
    	publishProgress("Start", "");
    	lockScreenOrientation();
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
				publishProgress("Exception", urls[i]);
				/*
				 * Si en algun momento vuelvo a tener esa parte activa, recordar que hacer una llamada a isBerlinWeekend  como esta, rompe
				 * la interfaz de EventLoaderFactory y hay que evitarlo. 
				// Distinguish the situation where the event is null because the Berlin Art Parasites website is not checked
				if(!(urls[i].equals("Berlin Art Parasites") && !ArtParasitesEventLoader.isBerlinWeekend())){
					System.out.println("Event is null");
					publishProgress("Exception", urls[i]);	
				}
				*/
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
			//Log.v(TAG,"El evento ya existe, no se añade.");
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
			displayToast(context.getString(R.string.searching));
		}else if (progress[0].equals("Exception")){
			displayToast("There were problems downloading the content from: " +progress[1] +" It's events won't be displayed.");
		}else if (progress[0].equals("Finish")){
			if (Integer.parseInt(progress[1]) > 0){
				displayToast("Added " +progress[1] +" new events, " +numOfEventsInDate +" for this day.");
			}else{
				displayToast(context.getString(R.string.no_new_events));
			}
		}
		//loadProgressBar.setProgress(progress[0].intValue());
	}
	
	/**
	* Goes to the Date Activity and hides the progressBar.
    */
	protected void onPostExecute(Integer result) {
		Log.d(TAG,"onPostExecute------------>");
		loadProgressBar.setVisibility(View.GONE);
		// Reload the Date Activity if there are new events
		unlockScreenOrientation();
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
		if (result > 0){
			Intent intent = new Intent(context, DateActivity.class);
			intent.putExtra(EXTRA_DATE, choosenDate);
			((Activity) context).startActivity(intent);
		}
	}
	
	private void lockScreenOrientation() {
	    int currentOrientation = context.getResources().getConfiguration().orientation;
	    if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
	    	((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    } else {
	    	((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    }
	}
	 
	private void unlockScreenOrientation() {
		((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}
	
	private void displayToast(final String message) {
	    //toast.cancel();
	    toast.setText(message); 
	    toast.setGravity(Gravity.TOP, 0, FirstTimeActivity.actionBarHeight);
	    toast.show();
	}
		
	private DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		}
		return databaseHelper;
	}
}
