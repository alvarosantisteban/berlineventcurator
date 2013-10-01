package com.alvarosantisteban.pathos.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alvarosantisteban.pathos.Event;
import com.alvarosantisteban.pathos.R;
import com.alvarosantisteban.pathos.utils.StringUtils;
import com.alvarosantisteban.pathos.utils.WebUtils;

import android.content.Context;
import android.util.Log;

/**
 * Receives the html from the I Heart Berlin website and parses the information to create a list of events.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class IHeartBerlinEventLoader implements EventLoader {
	
	public final static String WEBSITE_URL = "http://www.iheartberlin.de/events/";
	public final static String WEBSITE_NAME = "I Heart Berlin";
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "IHeartBerlinEventLoader";
	
	//Event's topic tags
	private String ART_TOPIC_TAG;
	//private String POLITICAL_TOPIC_TAG;
	private String GOING_OUT_TOPIC_TAG;
				
	// Event's type tags
	private String CONCERT_TYPE_TAG;
	private String PARTY_TYPE_TAG;
	private String EXHIBITION_TYPE_TAG;
	private String TALK_TYPE_TAG;
	private String SCREENING_TYPE_TAG;
	private String OTHER_TYPE_TAG;

	@Override
	public List<Event> load(Context context) {
		String html = WebUtils.downloadHtml(WEBSITE_URL, context);
		initializeTags(context);
		if(html.equals("Exception")){
			Log.w(TAG, "The html equals to Exception");
			return null;
		}
		try{
			return extractEventsFromIHeartBerlin(html);
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
		ART_TOPIC_TAG = context.getResources().getString(R.string.art_topic_tag);
		GOING_OUT_TOPIC_TAG = context.getResources().getString(R.string.goingout_topic_tag);
		
		CONCERT_TYPE_TAG = context.getResources().getString(R.string.concert_type_tag);
		PARTY_TYPE_TAG = context.getResources().getString(R.string.party_type_tag);
		TALK_TYPE_TAG = context.getResources().getString(R.string.talk_type_tag);
		EXHIBITION_TYPE_TAG = context.getResources().getString(R.string.exhibition_type_tag);
		SCREENING_TYPE_TAG = context.getResources().getString(R.string.screening_type_tag);
		OTHER_TYPE_TAG = context.getResources().getString(R.string.other_type_tag);
	}
	
	/**
	 * Creates a set of events from the html of the I Heart Berlin website. 
	 * Each Event has name, day, description and a link.
	 * 
	 * @param theHtml the String containing the html from the I Heart Berlin website
	 * @return a List of Event with the name, day and links set
	 */
	private List<Event> extractEventsFromIHeartBerlin(String theHtml) throws ArrayIndexOutOfBoundsException{  
		String myPattern = "<div class=\"event_date\">";
		String[] result = theHtml.split(myPattern);
		
		// Use an ArrayList because the number of events is unknown
		List<Event> events = new ArrayList<Event>(); 
		for (int i=1; i<result.length; i++){
			// Separate up to the first "</div>"
			String[] dateAndRest = result[i].split("</div>", 2);
			// Get the date
			String[] dayAndDate = dateAndRest[0].split(", ", 2);
			String[] eventsOfADay = dateAndRest[1].split("<div class=\"event_entry clearfix\">");
			for (int j=1; j<eventsOfADay.length; j++){
				Event event = new Event();
				// Format the date and set it
				event.setDay(StringUtils.formatDate(dayAndDate[1].replace(",", "").trim()));
				String[] imageAndRest = eventsOfADay[j].split("<div class=\"event_image\">");
				String[] imageAndRest2 = imageAndRest[1].split("<div class=\"event_info\">");
				// Set the image with all its html code 				
				event.setImage(imageAndRest2[0].replaceFirst("</div>", ""));
				String[] other = imageAndRest2[1].split("<div class=\"event_time\">");
				String[] timeAndRest = other[1].split("</div>",2);
				// Set the time when the event begins
				event.setHour(timeAndRest[0].replace("h", "").trim()); // Remove the "h" from 22:00h
				String[] nameAndRest = timeAndRest[1].split("<h3>");
				String[] nameAndRest2 = nameAndRest[1].split("</h3");
				// Set the name of the event
				event.setName(nameAndRest2[0].trim());
				String[] descriptionAndLinks = nameAndRest2[1].split("</p>");
				// Set the description
				String description = descriptionAndLinks[0].replaceFirst("<p>", "").replaceFirst(">", "").trim();
				event.setDescription(description);
				event.setLocation(extractLocation(description));
				// Set the links
				String[] trashAndLinks = descriptionAndLinks[1].split("<div class=\"event_links\">");
				// Check if there are links
				String[] htmlLink = trashAndLinks[1].split("<a href=\"");
				if(htmlLink.length > 0){
					String links = "";
					// Search for the links
					for (int z=0; z<htmlLink.length; z++){
						String[] pureLink = htmlLink[z].split("\"",2); // Get link
						links = pureLink[0].trim() +"\n" +links;
					}
					event.setLink(links);
				}
				// Set the origin
				event.setEventsOrigin(WEBSITE_NAME);
				// Set the origin's website
				event.setOriginsWebsite(WEBSITE_URL);
				// Set the thema tag
				event.setThemaTag(extractThemaTag(nameAndRest[0].trim()));
				// Set the type tag
				event.setTypeTag(extractTypeTag(nameAndRest[0].trim()));
				events.add(event);
			}
		}
		return events;
    }

	/**
	 * Extracts the thema tag assuming that the types "screening" "party" and "concert" belong to the tag "Going out" and all the others
	 * to the tag "Art".
	 * 
	 * @param tag the html with the keyword
	 * @return the thema tag (going out or art)
	 */
	private String extractThemaTag(String tag) {
		String lowerCase = tag.toLowerCase(Locale.getDefault());
		if (lowerCase.contains("movie") || lowerCase.contains("party") || lowerCase.contains("music")){
			return GOING_OUT_TOPIC_TAG;
		}else{ 
			return ART_TOPIC_TAG;
		}
	}

	/**
	 * Extracts the type tag looking for the keywords used by the creators of I Heart Berlin
	 * 
	 * @param text the html with the keyword
	 * @return the type tag 
	 */
	private String extractTypeTag(String text) {
		//System.out.println("text:"+text);
		if (text.toLowerCase(Locale.getDefault()).contains("movie")){
			return SCREENING_TYPE_TAG;
		}else if(text.toLowerCase(Locale.getDefault()).contains("party")){
			return PARTY_TYPE_TAG;
		}else if(text.toLowerCase(Locale.getDefault()).contains("art")){
			return EXHIBITION_TYPE_TAG;
		}else if(text.toLowerCase(Locale.getDefault()).contains("reading")){
			return TALK_TYPE_TAG;
		}else if(text.toLowerCase(Locale.getDefault()).contains("music")){
			return CONCERT_TYPE_TAG;
		}
		return OTHER_TYPE_TAG;
	}

	/**
	 * Extract the street name from the description.
	 * It does not get it if it's written like "NAME Str" or "NAMEstrasse".
	 * It also misses the possible letter that it might go after the number of the street.
	 * 
	 * @param description the string with the street name on it
	 * @return the street or an empty string if it was not found
	 */
	private String extractLocation(String description){
		String pattern="";
		int street = description.indexOf("str.");
		if(street > -1){
			pattern = "str.";
		}else {
			street = description.indexOf("damm");
			if(street > -1){
				pattern = "damm";
			}else{
				street = description.indexOf(" Str.");
				if(street > -1){
					pattern = " Str.";
				}else{
					street = description.indexOf("straße");
					if(street > -1){
						pattern = "straße";
					}else{
						street = description.indexOf("strasse");
						if(street > -1){
							pattern = "strasse";
						}else{
							street = description.indexOf("-Alle");
							if(street > -1){
								pattern = "-Alle";
							}else{
								street = description.indexOf("platz");
								if(street > -1){
									pattern = "platz";
								}else{
									street = description.indexOf("markt");
									if(street > -1){
										pattern = "markt";
									}
								}
							}
						}
					}
				}
			}
		}
		if (!pattern.equals("")){
			try{
				String streetName = description.substring(0, street);
				String streetNumber = description.substring(street);
				int startOfStreet = streetName.lastIndexOf(" ");
				int i = 0;
				while (!Character.isDigit(streetNumber.charAt(i))) i++;
				int j = i;
				while (j < streetNumber.length() && Character.isDigit(streetNumber.charAt(j))) j++;
				//Log.v(TAG, "startOfStreet:"+startOfStreet +" j:" +j);
				String fullStreet = description.substring(startOfStreet,street+j);
				return fullStreet +", Berlin";
			}catch(Exception e){
				Log.e(TAG, "Exception in extractLocation.\n" +e);
			}
		}
		return "";
	}
}