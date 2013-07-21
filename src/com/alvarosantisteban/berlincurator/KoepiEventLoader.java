package com.alvarosantisteban.berlincurator;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;

public class KoepiEventLoader implements EventLoader{
	
	public final static String websiteURL = "http://www.koepi137.net/eventskonzerte.php";
	public final static String webName = "Köpi's events";

	@Override
	public List<Event> load(Context context) {
		String html = WebUtils.downloadHtml(websiteURL, context);
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
			String[] dateAndRest = twoParts[1].split("</span><br />");
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
			event.setDescription(paragraphs[1]);
			
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
			event.setEventsOrigin(webName);
			events.add(event);
		}
		return events;
	}
}
