package com.alvarosantisteban.berlincurator;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;

public class StressFaktorEventLoader implements EventLoader {
	
	public final static String websiteURL = "http://stressfaktor.squat.net/termine.php?display=7";
	public final static String webName = "Stress Faktor";

	@Override
	public List<Event> load(Context context) {
		String html = WebUtils.downloadHtml(websiteURL, context);
		if(html.equals("Exception")){
			return null;
		}
		try{
			return extractEventsFromStressFaktor(html);
		}catch(ArrayIndexOutOfBoundsException exception){
			System.out.println("Exception catched!!!");
			return null;
		}
	}

	/**
	 * Creates a set of events from the html of the Stress Faktor website. 
	 * Each Event has name, day, time, a sometimes a description and a link.
	 * 
	 * @param theHtml the String containing the html from the Stress Faktor website
	 * @return a List of Event with the name, day, time, description and link
	 */
	private List<Event> extractEventsFromStressFaktor(String theHtml) throws ArrayIndexOutOfBoundsException {
		String[] eventsAndRest = theHtml.split("<!-- Ende Spalte 2 -->");
		String myPattern = "<table width=\"100%\" bgcolor=\"#000000\" cellpadding=\"3\" cellspacing=\"1\">";
		String[] result = eventsAndRest[0].split(myPattern);
		
		// Use an ArrayList because the number of events is unknown
		List<Event> events = new ArrayList<Event>(); 
		for (int i=1; i<result.length; i++){
			// Separate up to the first "<span class=text2>"
			String[] dateAndRest = result[i].split("<span class=text2>", 2);
			// Get the date
			String[] dayAndDate = dateAndRest[1].split("</b>", 2);
			String[] eventsOfADay = dateAndRest[1].split("<tr");
			for (int j=1; j<eventsOfADay.length; j++){
				Event event = new Event();
				// Format the date and set it
				String[] date = dayAndDate[0].split(", ");
				event.setDay(date[1].replace('.', '/').trim());
				String[] nothingTimeAndRest = eventsOfADay[j].split("<span class=text2>");
				String[] timeAndRest = nothingTimeAndRest[1].split(" ",2);	
				// Set the time
				event.setHour(timeAndRest[0].replace('.',':').trim());				
				String[] placeAndRest = nothingTimeAndRest[2].split("</b>",2);
				// Extract the location
				String[] place = extractPlace(placeAndRest[0]);
				// Set the location
				event.setLocation(place[0]);
				String[] nameAndRest = placeAndRest[1].split("<br>",2);
				// set the name
				event.setName(nameAndRest[0].replaceFirst(": ", "").trim());	
				String[] descriptionAndNothing = nameAndRest[1].split("</span>");
				// Extract the links
				String[] links = extractLinks(descriptionAndNothing[0]);
				// Set the links 
				event.setLinks(links);
				// Remove the image-links from the description
				String description = removeImageLinks(descriptionAndNothing[0]);
				// Set the description
				event.setDescription(description +place[1]);
				// Set the origin
				event.setEventsOrigin(webName);
				events.add(event);
			}
		}
		return events;		
	}


	/**
	 * Extracts the address and the place where the event from Stress Faktor will take place
	 * 
	 * @param maybeLink the string containing the information to be extracted
	 * @return a two positions string array with the street name and the place where it will take place
	 */
	private String[] extractPlace(String maybeLink) {	
		String address = "";
		String placeName = "";
		if(maybeLink.contains("<a href=\"http:")){
			String[] links = maybeLink.split("<a href=\"http:");
			String[] nothingAddress = links[1].split("title=\"");
			if(nothingAddress.length == 2){
				String[] AddressAndDescription = nothingAddress[1].split("\"", 2);
				String[] streetAndUbahn = AddressAndDescription[0].split(",", 2);
				address = streetAndUbahn[0];
				String[] placeAndDescription = AddressAndDescription[1].split("<",2);
				placeName =  "<br>(Takes place at the " +placeAndDescription[0].replace(">","") +")";
			}
		}else{
			String[] nothingAndPlace = maybeLink.split("<b>");
			address = address + nothingAndPlace[1];
		}
		//address = "<a href=\"https://maps.google.es/maps?q=" +address.replace(' ', '+') +",+Berlin\">" +address +"</a>";
		address = address +", Berlin";
		return new String[]{address, placeName};
	}
	
	/**
	 * Extracts the links that are at the end of the description
	 * 
	 * @param description the text with the description and the links 
	 * @return an array of strings with the links
	 */
	private String[] extractLinks(String description) {
		List<String> theLinks = new ArrayList<String>();
		if(description.contains("<a href=\"http:")){
			String[] links = description.split("<a href=\"http:");
			for (int z=1; z<links.length; z++){
				// Check if it is a link
				if (links[z].contains("title=\"Weitere Infos:")){
					String[] linkAndNothing = links[z].split("\"",2);
					// Set the links
					theLinks.add("http:" +linkAndNothing[0]);
				}
			}
		}
		String[] linksArray = new String[theLinks.size()];
		linksArray = theLinks.toArray(linksArray);
		return linksArray;
	}
	
	/**
	 * Removes the little images that contain links
	 * 
	 * @param description the text with the description and the image-links  
	 * @return a description without image-links 
	 */
	private String removeImageLinks(String description) {
		return description.replaceAll("<img src=\"images/infoicon.gif\" width=\"15\" height=\"15\" border=\"0\" align=\"top\">", "");
	}

}
