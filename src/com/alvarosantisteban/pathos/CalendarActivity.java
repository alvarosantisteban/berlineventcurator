package com.alvarosantisteban.pathos;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.Toast;
import com.alvarosantisteban.pathos.R;

/**
 * Displays a CalendarView to allow the user to select the day from which the events are going to be displayed in DateActivity.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class CalendarActivity extends Activity {
	
	// Constants and Preferences
	private final String LAST_CHOOSEN_DATE = "lastChoosenDate";
	public static final String EXTRA_DATE = "date";
	
	private Context context = this;
	private SharedPreferences sharedPref;
	
	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
	private long lastDate;
	private CalendarView calendar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar);
		
		// Enable the app's icon to act as home
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
		calendar = (CalendarView) findViewById(R.id.calendarView);
		
		// Get the default shared preferences
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Get the last selected day
		Calendar today = new GregorianCalendar();
		lastDate = sharedPref.getLong(LAST_CHOOSEN_DATE, today.getTimeInMillis());
		calendar.setDate(lastDate);
		
		// Detect the change of the selected date
		calendar.setOnDateChangeListener(new OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                    int dayOfMonth) {
            	// Inform the user of the selected date
            	Toast toast = Toast.makeText(getApplicationContext(), "You selected to see the events for the day: "+dayOfMonth +"/" +(++month) +"/" +year, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, FirstTimeActivity.actionBarHeight);
				toast.show();
				       
				// Save the selected date 
                Calendar selectedDate = new GregorianCalendar(year, month-1, dayOfMonth);
                long dateInMilliseconds = selectedDate.getTimeInMillis();
                Editor editor = sharedPref.edit();
                editor.putLong(LAST_CHOOSEN_DATE, dateInMilliseconds);
                editor.commit();
                 
                // Go to DateActivity for the selected date
                String choosenDate = dateFormat.format(selectedDate.getTime());
                Intent intent = new Intent(context, DateActivity.class);
                intent.putExtra(EXTRA_DATE, choosenDate);
             	startActivity(intent);
            }
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calendar, menu);
		return true;
	}

	/**
	 * Checks which item from the menu has been clicked
	 */
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
			// app icon in action bar clicked; go to the DateActivity
			Intent intent = new Intent(this, DateActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
        return true;
    }	
}