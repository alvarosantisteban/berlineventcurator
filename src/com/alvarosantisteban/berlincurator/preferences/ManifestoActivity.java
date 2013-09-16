package com.alvarosantisteban.berlincurator.preferences;

import com.alvarosantisteban.berlincurator.R;
import android.os.Build;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Displays the app's manifesto.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class ManifestoActivity extends Activity {

	//LinearLayout layout;
	RelativeLayout layout;
	TextView manifesto;
	TextView pathosDefinition;
	//Point p; //The "x" and "y" position of the "Show Button" on screen.
	PopupWindow popup;
	
	private final String TAG = "ManifestoActivity";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manifesto);
		// Enable the app's icon to act as home
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		//layout = (LinearLayout) findViewById(R.id.manifesto_layout);
		layout = (RelativeLayout) findViewById(R.id.manifesto_layout);
		manifesto = (TextView) findViewById(R.id.manifesto);
		pathosDefinition = (TextView) findViewById(R.id.pathos_word);
		pathosDefinition.setTextColor(Color.DKGRAY);
		popup = null;
		
		pathosDefinition.setOnClickListener(new OnClickListener() {
		     @Override
		     public void onClick(View arg0) {
		    	 showPopup(ManifestoActivity.this, arg0.getX(), arg0.getY());
		     }
		   });
	}
	
	private void showPopup(final Activity context, float x, float y) {	
		   int popupWidth = 550;
		   int popupHeight = 400;
		   // Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
		   int OFFSET_X = 215;
		   int OFFSET_Y = 280;
		   int pX = 0;
		   int pY = 0;
		   if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			   pX = (int) x - OFFSET_X;
			   pY = (int) y - OFFSET_Y;
		   }else{
			   pX = (int) x - OFFSET_Y;
			   pY = (int) y - OFFSET_X -230;
		   }
		 
		   // Inflate the popup_layout.xml
		   LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
		   LayoutInflater layoutInflater = (LayoutInflater) context
		     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		   View layout = layoutInflater.inflate(R.layout.popup_layout, viewGroup);
		 
		   // Creating the PopupWindow
		   popup = new PopupWindow(this);
		   popup.setContentView(layout);
		   popup.setWidth(popupWidth);
		   popup.setHeight(popupHeight);
		   popup.setFocusable(true);
		 
		   // Displaying the popup
		   popup.showAtLocation(layout, Gravity.NO_GRAVITY, pX, pY);
		}

	public void onDestroy() {
	    super.onDestroy();
	    if (popup != null){
	    	popup.dismiss();
	    }
	    Log.d(TAG, "In the onDestroy()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}
	
	/**
	 * Checks which item from the menu has been clicked
	 */
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
			// app icon in action bar clicked; go to Settings
			Intent intent = new Intent(this, SettingsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
 
        return true;
    }
}
