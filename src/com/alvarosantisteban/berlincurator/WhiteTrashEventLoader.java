package com.alvarosantisteban.berlincurator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;

public class WhiteTrashEventLoader implements EventLoader{
	
	public final static String websiteURL = "http://www.whitetrashfastfood.com/events/";
	public final static String webName = "White Trash";

	@Override
	public List<Event> load(Context context) {
		String html = WebUtils.downloadHtml(websiteURL, context);
		if(html.equals("Exception")){
			return null;
		}try{
			return extractEventsFromWhiteTrash(html);
		}catch(ArrayIndexOutOfBoundsException exception){
			System.out.println("Exception catched!!!");
			return null;
		}
	}
	
	/**
	 * Creates a set of events from the html of the White Trash website. 
	 * Each Event has name, day, description and a link.
	 * 
	 * @param theHtml the String containing the html from the White Trash website
	 * @return a List of Event with the name, day, hour and link set
	 */
	private List<Event> extractEventsFromWhiteTrash(String theHtml) throws ArrayIndexOutOfBoundsException{  
		String myPattern = "<h4>";
		String[] result = theHtml.split(myPattern);
		
		// Use an ArrayList because the number of events is unknown (most likely theew will be 7)
		List<Event> events = new ArrayList<Event>(); 
		for (int i=1; i<result.length; i++){
			// Separate up to the first "</a>"
			String[] dateAndRest = result[i].split("</a", 2);
			// Get the date
			String[] dayAndDate = dateAndRest[0].split("\">", 2);
			String[] eventsOfADay = dateAndRest[1].split("<p class=\"time\">");
			for (int j=1; j<eventsOfADay.length; j++){
				Event event = new Event();
				// Format the date and set it
				event.setDay(Utils.formatDate(dayAndDate[1]));
				String[] timeAndRest = eventsOfADay[j].split("</p>",2);
				// Set the time
				//System.out.println("timeAndRest[0]" +timeAndRest[0]);
				event.setHour(timeAndRest[0].replace("h", "").trim());
				String[] linkNameAndRest = timeAndRest[1].split("<a href=\"");
				String[] linkNameAndRest2 = linkNameAndRest[1].split("\">",2);
				// Make the relative link absolut and set it
				event.setLink("http://www.whitetrashfastfood.com"+linkNameAndRest2[0]); 
				String description = "";
				String[]nameAndRest = linkNameAndRest2[1].split("\\(",2);
				// Set the name depending if the band name contains "(" or not
				if (nameAndRest[0].equals(linkNameAndRest2[1])){
					String[] nameAndBlaBla = linkNameAndRest2[1].split("</a>",2);
					event.setName(nameAndBlaBla[0]);
				}else{
					event.setName(nameAndRest[0]);
				}
				String[] description1 = linkNameAndRest2[1].split("</a>");
				// Set the constructed description
				description = description1[0];
				String[] description3 = description1[1].split("<br>"); // The place is in description3[0]
				String[] description2a = description1[1].split("<span class=\"summ\">");
				String[] description2b = description2a[1].split("</span>");
				event.setDescription(description + description2b[0] + description3[0]);
				
				// Set the location
				//event.setLocation("<a href=\"https://maps.google.es/maps?q=Schoenhauser+Allee+6-7,+Berlin\">White Trash Bar</a>");
				event.setLocation("White Trash, Schoenhauser Allee 6-7, Berlin");
				// Set the origin
				event.setEventsOrigin(webName);
				// Set the thema tag
				event.setThemaTag(DateActivity.GOING_OUT_THEMA_TAG);
				// Set the type tag
				event.setTypeTag(extractTypeTag(nameAndRest[0]));
				events.add(event);
			}
		}
		return events;
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
		if (lowerCase.contains("live")){
			return DateActivity.CONCERT_TYPE_TAG;
		}
		return DateActivity.PARTY_TYPE_TAG;
	}
}