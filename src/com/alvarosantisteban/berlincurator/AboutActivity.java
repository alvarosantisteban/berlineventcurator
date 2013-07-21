package com.alvarosantisteban.berlincurator;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends Activity {
	
	TextView email;
	TextView gitHubUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		// Enable the app's icon to act as home
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    email = (TextView) findViewById(R.id.email_address);
	    email.setMovementMethod(LinkMovementMethod.getInstance());
	    email.setText(Html.fromHtml("<a href=\"mailto:alvarosantisteban@gmail.com?Subject=BerlinCurator\">alvarosantisteban@gmail.com</a>"));
	    
	    gitHubUrl = (TextView) findViewById(R.id.codeUrl);
	    gitHubUrl.setMovementMethod(LinkMovementMethod.getInstance());
	    gitHubUrl.setText(Html.fromHtml("<a href=\"https://github.com/alvarosantisteban/curator\">My GitHub account</a>"));
	    
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
