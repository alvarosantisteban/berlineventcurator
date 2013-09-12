package com.alvarosantisteban.berlincurator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
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
 * Activity that is shown just one time, the first time that the app is used. It shows a small animation while downloading the events.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class FirstTimeActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "FirstTimeActivity";
	
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
	 * The animation displayed while the events are downloaded
	 */
	public AnimationDrawable wavesAnimation;

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
	 * Loads the elements from the resources
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		context = this;
		setContentView(R.layout.activity_first_time);
		// Get the default shared preferences
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);		 
		
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
		websNames = setOfWebsites.toArray(new String[0]);	
		
		// Get the height of the action bar
		TypedValue tv = new TypedValue();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
			actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);
		} 
		
		// Prepare the animation
		ImageView waveImage = (ImageView) findViewById(R.id.wave);
		waveImage.setBackgroundResource(R.drawable.building_waves);
		wavesAnimation = (AnimationDrawable) waveImage.getBackground();	
	}

	/**
	 * Start the animation and the download of the websites
	 */
	public boolean onTouchEvent(MotionEvent event) {
		Log.d(TAG, "onTouchEvent");
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// Mark that the App has been used
			Editor editor = sharedPref.edit();
			editor.putBoolean("isFirstTimeApp", false);
			editor.commit();
			
			// Start the animation
			wavesAnimation.start();
			
			// Inform the user
			Toast toast = Toast.makeText(getBaseContext(), "Downloading the events.\nIt might take a few seconds, be patient ;)", Toast.LENGTH_LONG);
	    	toast.setGravity(Gravity.TOP, 0, actionBarHeight);
	    	toast.show();
	    	
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		    // Check if is possible to establish a connection
		    if (networkInfo != null && networkInfo.isConnected()) {
		    	Log.i(TAG, "There is a network connection available.");
		    	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
		    	DownloadWebpageAsyncTask download = new DownloadWebpageAsyncTask(context, null, dateFormat.format(Calendar.getInstance().getTime()));
				// Execute the asyncronous task of downloading the websites
				download.execute(websNames);
		    } else {
		    	// Inform the user that there is no network connection available
		    	toast = Toast.makeText(getBaseContext(), "No network connection available.", Toast.LENGTH_LONG);
		    	toast.setGravity(Gravity.TOP, 0, actionBarHeight);
		    	toast.show();
		        Log.w(TAG, "No network connection available.");
		    }
		    return true;
		}
		return super.onTouchEvent(event);
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
		Log.d(TAG, "In the onResume() event");
	}
	    
	public void onPause() {
	    super.onPause();
	    Log.d(TAG, "In the onPause() event");
	}
	    
	public void onStop() {
	    super.onStop();
	    Log.d(TAG, "In the onStop() event");
	}
	    
	public void onDestroy() {
	    super.onDestroy();
	    Log.d(TAG, "In the onDestroy() event");
	}
}