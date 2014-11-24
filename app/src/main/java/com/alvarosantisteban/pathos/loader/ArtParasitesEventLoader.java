package com.alvarosantisteban.pathos.loader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.alvarosantisteban.pathos.Event;
import com.alvarosantisteban.pathos.utils.StringUtils;
import com.alvarosantisteban.pathos.utils.WebUtils;

import android.content.Context;
import android.util.Log;

/**
 * NOT USED ANYMORE
 * Receives the html from the ArtParasites website and parses the information to create a list of events.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class ArtParasitesEventLoader implements EventLoader {
	
	public final static String WEBSITE_URL = "http://www.berlin-artparasites.com/recommended";
	public final static String WEBSITE_NAME = "Berlin Art Parasites";
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "ArtParasitesEventLoader";

	@Override
	public List<Event> load(Context context) {	
		if(isBerlinWeekend()){
			String html = WebUtils.downloadHtml(WEBSITE_URL, context);
			Log.v(TAG, "Is Berlin Weeeeeeekend");
			try{
				String subUrl = extractUrlFromMainArtParasites(html);
				html = WebUtils.downloadHtml(subUrl, context);
				return extractEventsFromArtParasites(html);
			}catch(ArrayIndexOutOfBoundsException e){
				Log.e(TAG, "Array index out of bounds" +e);
				return null;
			}
		}
		return null;
	}

	/**
	 * The weekend in Berlin starts on Thursday. This function checks if today is thursday, friday, saturday or sunday.
	 * 
	 * @return true if the day when the function is running is thursday, friday, saturday or sunday. False otherwise.
	 */
	public static boolean isBerlinWeekend() {
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK); 
		return (day== Calendar.THURSDAY || day == Calendar.FRIDAY || day == Calendar.SATURDAY || day == Calendar.SUNDAY);
	}


	/**
	 * Creates a set of events from the html of the Berlin Art Parasites website. 
	 * Each Event has name, day, description and a link.
	 * 
	 * @param theHtml the String containing the html from the Berlin Art Parasites website
	 * @return a List of Event with the name, day, hour, description and links set
	 */
	private List<Event> extractEventsFromArtParasites(String theHtml) throws ArrayIndexOutOfBoundsException{  
		// First, get rid of everthing that goes after <em>
		String[] clearnerHtml = theHtml.split("Article by",2);
		String myPattern = "<strong"; // Marks the number of days
		String[] result = clearnerHtml[0].split(myPattern);
		
		// Use an ArrayList because the number of events per day is unknown
		List<Event> events = new ArrayList<Event>(); 
		for (int i=1; i<result.length; i++){
			// Separate up to the first "</strong>"
			String[] dateAndRest = result[i].split("</strong>", 2);
			// Get the date
			String[] dayAndDate = dateAndRest[0].split(" ", 2);
			String[] eventsOfADay = dateAndRest[1].split("<a href=\""); // Marks the number of events
			for (int j=1; j<=(eventsOfADay.length-1)/2; j++){
				Event event = new Event();
				// Format the date and set it
				String day = StringUtils.formatDate(dayAndDate[1].replace(",", "").trim());
				event.setDay(day);
				
				Log.d(TAG, "eventsOfADay[(j*2)-1]"+eventsOfADay[(j*2)-1]);
				String[] linkAndPlace = eventsOfADay[(j*2)-1].split("</a>",2);
				String[] place = linkAndPlace[0].split("\">"); 
				// Extract the location
				//event.setLocation("https://maps.google.es/maps?q="+place[1].replace(' ', '+')+",+Berlin");
				event.setLocation("<a href=\"https://maps.google.es/maps?q="+place[1].replace(' ', '+')+",+Berlin\">"+place[1]+"</a>");
				String placeLink = place[0];
				// Check if the link still contains crap
				if(placeLink.contains("\"")){
					String[] cleanLink = placeLink.split("\"");
					placeLink = cleanLink[0];
				}
				// Check if the link belongs to the Art Parasites site or not
				if(placeLink.charAt(0) == '/'){
					event.setLink("http://www.berlin-artparasites.com"+placeLink);
				}else{
					String[] linkAndCrap = placeLink.split("\"",2);
					event.setLink(linkAndCrap[0]);
				}
				
				// Extract the name and a link and set them
				String[] linkNameAndRest = eventsOfADay[(j*2)].split("</a>",2);
				String[] linkAndName = linkNameAndRest[0].split("\">"); 
				Log.d(TAG, "name:" +linkAndName[1]);
				event.setName(linkAndName[1]);
				
				// Extract the links
				event.setLink(extractLinks(linkAndName[0]));
				
				String[] timeAndRest = linkNameAndRest[1].split("</p>",2);
				
				// Extract the hour
				event.setHour(StringUtils.extractTime(timeAndRest[0]));
				
				String[] nothingAndDescription = timeAndRest[1].split(">", 2);
				String[] descriptionAndNothing = nothingAndDescription[1].split("</p>",2);
				
				// Create the description and set it
				String description = linkAndName[1] + " at the " +place[1] +":" +"<br>" + descriptionAndNothing[0].trim();
				Log.d(TAG, "description:" +description);
				event.setDescription(description);
				
				// Set the origin
				event.setEventsOrigin(WEBSITE_NAME);
				// Set the origin's website
				event.setOriginsWebsite(WEBSITE_URL);
				events.add(event);
			}
		}
		return events;
    }
	
	/**
	 *  A small separate function to extract the links from the html mess
	 *  
	 * @param theLinks the String with the links
	 * @return the url with the link
	 */
	private String extractLinks(String theLinks) {
		String url="";
		// Check if the link still contains crap
		if(theLinks.contains("\"")){
			String[] cleanLink = theLinks.split("\"");
			theLinks = cleanLink[0];
		}
		// Check if the link belongs to the Art Parasites site or not
		if(theLinks.charAt(0) == '/'){
			url="http://www.berlin-artparasites.com"+theLinks;
		}else{
			String[] linkAndCrap = theLinks.split("\"",2);
			url=linkAndCrap[0];
		}
		return url;
	}


	/**
	 * Reads the html from the main site of the Berlin Art Parasites website and looks for the first entry of the "Best Weekend Art Events" so it
	 * can get the url of the site which contains the events.
	 * 
	 * @param parasitesMainSite the html of the main site of Berlin Art Parasites
	 * @return the url of the site with the events for the weekend
	 */
	private String extractUrlFromMainArtParasites(String parasitesMainSite) throws ArrayIndexOutOfBoundsException{
		
		// Look for the first entry of Best Weekend Art Events
		String myPattern = "recommended art events";
		String[] result = parasitesMainSite.toLowerCase(Locale.GERMAN).split(myPattern,2);
		//System.out.println("result[1]"+result[1]);
		// Set the left limit of the link
		String[] links = result[1].split("<h1><a href=\"",2);
		//System.out.println("links[0]"+links[0]);
		// Set the right limit of the link
		String[] link = links[1].split("\">",2);
		// Return the absolute link
		return "http://www.berlin-artparasites.com"+link[0];	
		
	}
}