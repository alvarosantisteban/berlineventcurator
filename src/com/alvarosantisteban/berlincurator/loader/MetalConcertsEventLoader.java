package com.alvarosantisteban.berlincurator.loader;

import java.util.ArrayList;
import java.util.List;

import com.alvarosantisteban.berlincurator.Event;
import com.alvarosantisteban.berlincurator.R;
import com.alvarosantisteban.berlincurator.utils.WebUtils;

import android.content.Context;
import android.util.Log;

/**
 * Receives the html from the Metal Concerts website and parses the information to create a list of events.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class MetalConcertsEventLoader implements EventLoader{
	
	public final static String WEBSITE_URL = "http://berlinmetal.lima-city.de/index.php/index.php?id=start";
	public final static String WEBSITE_NAME = "Metal Concerts";
	private final static String ACTUAL_YEAR = "2013";
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "MetalConcertsEventLoader";
	
	//Event's topic tags
	private String GOING_OUT_TOPIC_TAG;
				
	// Event's type tags
	private String CONCERT_TYPE_TAG;

	@Override
	public List<Event> load(Context context) {
		String html = WebUtils.downloadHtml(WEBSITE_URL, context);
		initializeTags(context);
		if(html.equals("Exception")){
			Log.w(TAG, "The html equals to Exception");
			return null;
		}
		try{
			return extractEventsFromMetalConcerts(html);
		}catch(ArrayIndexOutOfBoundsException e){
			Log.e(TAG, "ArrayIndexOutOfBoundsException" +e);
			return null;
		}
	}
	
	/**
	 * Initialize the tags used to categorize the topic and the type of each event
	 * 
	 * @param context
	 */
	private void initializeTags(Context context) {
		GOING_OUT_TOPIC_TAG = context.getResources().getString(R.string.goingout_topic_tag);
		
		CONCERT_TYPE_TAG = context.getResources().getString(R.string.concert_type_tag);
	}

	/**
	 * Creates a set of events from the html of the Metal Concerts website. 
	 * Each Event has name, day, description and a link.
	 * 
	 * @param theHtml the String containing the html from the Metal Concerts website
	 * @return a List of Event with the name, day and links set
	 */
	private List<Event> extractEventsFromMetalConcerts(String theHtml) throws ArrayIndexOutOfBoundsException{  
		String myPattern = "<p class=\"konzerte\">"; //<p class=\"konzerte\">.*?</p>
		String[] result = theHtml.split(myPattern);
		
		// Throw away the first entry of the array because it does not contain a concert
		List<Event> events = new ArrayList<Event>(result.length-1); 
		for (int i=1; i<result.length; i++){
			// Separate up to the "@"
			String[] twoParts = result[i].split("@");
			// Separate the date and the name of the band
			String[] dateAndName = twoParts[0].split("\\. ");
			Event event = new Event();
			event.setName(dateAndName[1]);
			String eventDate = dateAndName[0].replace('.', '/');
			//DateFormat dateFormat = new SimpleDateFormat("yyyy", Locale.GERMAN);
			//eventDate = eventDate.concat("/" +dateFormat.format(calendar.getTime()));
			event.setDay(eventDate + "/" +ACTUAL_YEAR);
			
			// Remove useless code
			String htmlLink = twoParts[1].replaceFirst("</p>", "");
			// Check if there is a link
			if(htmlLink.charAt(0) == '<'){
				// Remove useless code
				htmlLink = htmlLink.replaceFirst("</a>", "");
				String[] pureLink = htmlLink.split("\""); // Get link
				String[] concertPlace = htmlLink.split(">"); // Get name
				event.setLink(pureLink[1]);
				event.setDescription("The concert will take place at the " +concertPlace[1]);
				event.setLocation("<a href=\"https://maps.google.es/maps?q=" +concertPlace[1].replace(' ', '+') +",+Berlin\">" +concertPlace[1] +"</a>");
			}else{
				String concertPlace = htmlLink;
				event.setDescription("The concert will take place at the " +concertPlace);
				event.setLocation("<a href=\"https://maps.google.es/maps?q=" +concertPlace.replace(' ', '+') +",+Berlin\">" +concertPlace +"</a>");
			}
			// Set the origin
			event.setEventsOrigin(WEBSITE_NAME);
			// Set the origin's website
			event.setOriginsWebsite(WEBSITE_URL);
			// Set the thema tag
			event.setThemaTag(GOING_OUT_TOPIC_TAG);
			// Set the type tag
			event.setTypeTag(CONCERT_TYPE_TAG);
			events.add(event);
		}
		return events;
    }
}
