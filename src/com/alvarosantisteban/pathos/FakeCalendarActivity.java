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
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.Toast;
import com.alvarosantisteban.pathos.R;

/**
 * Displays a date picker to select a date.
 * Meant to be used just for devices that don't have an API of 11, in which case a CalendarView is displayed.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class FakeCalendarActivity extends Activity{

private final String LAST_CHOOSEN_DATE = "lastChoosenDate";
	
	private DatePicker picker;
	Context context = this;
	public static final String EXTRA_DATE = "date";
	
	private SharedPreferences sharedPref;
	
	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fake_calendar);
		
		// Enable the app's icon to act as home
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
		picker = (DatePicker) findViewById(R.id.datePicker);
		
		// Get the default shared preferences
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		Calendar selectedDay = new GregorianCalendar();
		long lastDate = sharedPref.getLong(LAST_CHOOSEN_DATE, selectedDay.getTimeInMillis());
		selectedDay.setTimeInMillis(lastDate);
		
		// Detects the change of the date
		picker.init(selectedDay.get(Calendar.YEAR),selectedDay.get(Calendar.MONTH),
				selectedDay.get(Calendar.DATE),(new OnDateChangedListener() {

			@Override
			public void onDateChanged(DatePicker arg0, int year, int month,
                    int dayOfMonth) {
				Toast toast = Toast.makeText(getApplicationContext(), "You selected to see the events for the day: "+dayOfMonth +"/" +(++month) +"/" +year, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, FirstTimeActivity.actionBarHeight);
				 toast.show();
				       
                Calendar selectedDate = new GregorianCalendar(year, month-1, dayOfMonth);
                long dateInMilliseconds = selectedDate.getTimeInMillis();
                Editor editor = sharedPref.edit();
                editor.putLong(LAST_CHOOSEN_DATE, dateInMilliseconds);
                editor.commit();
                
                String choosenDate = dateFormat.format(selectedDate.getTime());
                Intent intent = new Intent(context, DateActivity.class);
                intent.putExtra(EXTRA_DATE, choosenDate);
            	startActivity(intent);
				
			}
        }));
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
