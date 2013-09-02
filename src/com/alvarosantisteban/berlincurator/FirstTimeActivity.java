package com.alvarosantisteban.berlincurator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
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

import com.alvarosantisteban.berlincurator.loader.GothDatumEventLoader;
import com.alvarosantisteban.berlincurator.loader.IHeartBerlinEventLoader;
import com.alvarosantisteban.berlincurator.loader.IndexEventLoader;
import com.alvarosantisteban.berlincurator.loader.MetalConcertsEventLoader;
import com.alvarosantisteban.berlincurator.loader.StressFaktorEventLoader;
import com.alvarosantisteban.berlincurator.loader.WhiteTrashEventLoader;
import com.alvarosantisteban.berlincurator.preferences.SettingsActivity;
import com.alvarosantisteban.berlincurator.preferences.SettingsFragment;
import com.alvarosantisteban.berlincurator.utils.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

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
										MetalConcertsEventLoader.WEBSITE_NAME, 
										WhiteTrashEventLoader.WEBSITE_NAME, 
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
    
    //private static Toast toast;
	
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
		//toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		
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
		Set<String> setOfWebsites = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setOfWebsites = sharedPref.getStringSet(SettingsFragment.KEY_PREF_MULTILIST_SITES, new HashSet<String>(Arrays.asList(context.getResources().getStringArray(R.array.default_sites_array))));
		} else {
			String s = sharedPref.getString(SettingsFragment.KEY_PREF_MULTILIST_SITES, context.getResources().getString(R.string.sites_pseudoarray_values));
			if(s != null){
				setOfWebsites = new HashSet<String>(Arrays.asList(s.split(",")));
			}
		}
		//Set<String> setOfWebsites = sharedPref.getStringSet(SettingsFragment.KEY_PREF_MULTILIST_SITES, new HashSet<String>(Arrays.asList(context.getResources().getStringArray(R.array.default_sites_array))));
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
			actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);
		} 
		//context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
		//actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);

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
					//DownloadWebpageTask download = new DownloadWebpageTask();
			    	DownloadWebpageAsyncTask download = new DownloadWebpageAsyncTask(context, loadProgressBar);
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
}