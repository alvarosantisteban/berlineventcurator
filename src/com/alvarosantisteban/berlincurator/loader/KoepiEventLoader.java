package com.alvarosantisteban.berlincurator.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.alvarosantisteban.berlincurator.Event;
import com.alvarosantisteban.berlincurator.R;
import com.alvarosantisteban.berlincurator.utils.WebUtils;

import android.content.Context;

public class KoepiEventLoader implements EventLoader{
	
	public final static String WEBSITE_URL = "http://www.koepi137.net/eventskonzerte.php";
	public final static String WEBSITE_NAME = "Köpi's events";
	
	//Event's topic tags
	private String ART_TOPIC_TAG;
	//private String POLITICAL_TOPIC_TAG;
	private String GOING_OUT_TOPIC_TAG;
				
	// Event's type tags
	private String CONCERT_TYPE_TAG;
	private String PARTY_TYPE_TAG;
	private String EXHIBITION_TYPE_TAG;
	//private String TALK_TYPE_TAG;
	//private String SCREENING_TYPE_TAG;
	private String OTHER_TYPE_TAG;

	@Override
	public List<Event> load(Context context) {
		String html = WebUtils.downloadHtml(WEBSITE_URL, context);
		initializeTags(context);
		if(html.equals("Exception")){
			return null;
		}
		try {
			return extractEventsFromKoepi(html);
		}catch(ArrayIndexOutOfBoundsException exception){
			System.out.println("Exception catched!!!");
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
		EXHIBITION_TYPE_TAG = context.getResources().getString(R.string.exhibition_type_tag);
		OTHER_TYPE_TAG = context.getResources().getString(R.string.other_type_tag);
	}
	
	/**
	 * Creates a set of events from the html of the Koepi website. 
	 * Each Event has name, day, time, a description and sometimes a link.
	 * 
	 * @param theHtml the String containing the html from the Koepi website
	 * @return a List of Event with the name, day and links set
	 */
	private List<Event> extractEventsFromKoepi(String theHtml) throws ArrayIndexOutOfBoundsException{
		String[] uselessAndGood = theHtml.split("</div -->");
		String myPattern = "<span class=\"datum\">"; //<p class=\"konzerte\">.*?</p>
		String[] result = uselessAndGood[1].split(myPattern);
		
		// Throw away the first entry of the array because it does not contain an event
		List<Event> events = new ArrayList<Event>(result.length-1); 
		for (int i=1; i<result.length; i++){
			Event event = new Event();
			// Remove the left part of ", "
			String[] twoParts = result[i].split(", ", 2); // We want just the first
			// Remove useless code
			String[] dateAndRest = twoParts[1].split("</span><br />",2);
			// Format the date and set it to the event
			event.setDay(dateAndRest[0].replace('.', '/'));
			// Separate the first paragraph
			String[] paragraphs = dateAndRest[1].split("<br />", 2);
			// Get the time and name
			String[] hourAndName = paragraphs[0].split(" Uhr: ", 2);
			// Set the time to the event
			event.setHour(hourAndName[0].trim());
			// Set the name to the event
			event.setName(hourAndName[1]);
			// Set the description (with lots of html code)
			String descriptionWithoutExtraLine = paragraphs[1].trim();
			int lastNewLine = descriptionWithoutExtraLine.lastIndexOf("<br />");
			descriptionWithoutExtraLine = descriptionWithoutExtraLine.substring(0, lastNewLine).trim();
			event.setDescription(descriptionWithoutExtraLine);
			
			// Set the location
			//event.setLocation("<a href=\"https://maps.google.es/maps?q=Koepenicker+139,+Berlin\">Köpi</a>");
			event.setLocation("Koepenicker 139, Berlin");
			
			// Check if there is a link
			String[] htmlLink = twoParts[1].split("<a href=\"", 2);
			if(htmlLink.length == 2){
				// Remove useless code
				String[] pureLink = htmlLink[1].split("\"",2); // Get link
				event.setLink(pureLink[0]);
			}			
			// Set the origin
			event.setEventsOrigin(WEBSITE_NAME);
			// Set the origin's website
			event.setOriginsWebsite(WEBSITE_URL);
			// Set the thema tag
			event.setThemaTag(extractThemaTag(hourAndName[1]));
			// Set the type tag
			event.setTypeTag(extractTypeTag(hourAndName[1]));
			events.add(event);
		}
		return events;
	}
	
	/**
	 * Extracts the thema tag assuming that the type "exhibition" belong to the tag "Art" and all the others
	 * to the tag "Going out".
	 * 
	 * @param tag the html with the keyword
	 * @return the thema tag (going out or art)
	 */
	private String extractThemaTag(String tag) {
		String lowerCase = tag.toLowerCase(Locale.getDefault());
		if (lowerCase.contains("exhibition")){
			return ART_TOPIC_TAG;
		}else{ 
			return GOING_OUT_TOPIC_TAG;
		}
	}

	/**
	 * Extracts the type tag looking for some keywords. Some keywords such as Vöku, could also be easily recognized but still belong to
	 * the "Other" type tag.
	 * 
	 * @param text the html with the keyword
	 * @return the type tag 
	 */
	private String extractTypeTag(String text) {
		String lowerCase = text.toLowerCase(Locale.getDefault());
		if (lowerCase.contains("konzert") || lowerCase.contains("solikonzert") || lowerCase.contains("festival") || lowerCase.contains("20 jahre agh")){
			return CONCERT_TYPE_TAG;
		}else if(lowerCase.contains("party") || lowerCase.contains("soliparty") || lowerCase.contains("soli-technoparty")){
			return PARTY_TYPE_TAG;
		}else if(lowerCase.contains("exhibition")){
			return EXHIBITION_TYPE_TAG;
		}
		return OTHER_TYPE_TAG;
	}

}
