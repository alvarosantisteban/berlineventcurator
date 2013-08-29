package com.alvarosantisteban.berlincurator;

import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Utils {
	
	/**
	 * Converts a string of time in the format of 12hours HH:MMa to 24 hours 
	 * 
	 * @param timeIn12Hours the time written in 12 hours
	 * @return a string with the time in 24 hours HH:MM or an empty string if there was a problem.
	 */
	public static String convertTo24Hours(String timeIn12Hours){
		SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
	    SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mma", Locale.ENGLISH);
	    try {
			Date date = parseFormat.parse(timeIn12Hours);
			return displayFormat.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 *  A small separate function to extract the hour from the html mess.
	 *  
	 * @param theTime the String with the time
	 * @return the time in the format HH:MM or HH:MM-HH:MM or an empty string if there was no time or a problem arose.
	 */
	public static String extractTime(String theTime) {
		//System.out.println("|"+theTime +"|");
		theTime = theTime.replace(".", ":");
		//System.out.println("|"+theTime +"|");
		String timePeriod = "";
		// Make sure that there is a time
		if (theTime.contains("pm")){
			timePeriod = "pm";
		}else if (theTime.contains("am")){
			timePeriod = "am";
		}else{
			// Is not a time
			System.out.println("It's not a time. We return an empty string");
			return "";
		}
		// Extract the time and set it
		String[] timeAndRest = theTime.split(timePeriod); 
					
		// Search for a digit
		int z = 0;
		while (!Character.isDigit(timeAndRest[0].charAt(z))) z++;
		String hour = timeAndRest[0].substring(z);
		hour = hour.trim();
		String timeSeparator = "";
		// Check if there is a time interval by looking at a time separator
		if (hour.contains("-")){
			timeSeparator = "-";
		}else if (hour.contains("–")){
			timeSeparator = "–";
		}else{	
			String hour24;
			if (hour.length() == 1){
				hour24 = Utils.convertTo24Hours("0"+hour+":00"+timePeriod);
			}else if(hour.length() == 4){
				hour24 = Utils.convertTo24Hours("0"+hour+timePeriod);
			}else if(hour.length() == 2){
				hour24 = Utils.convertTo24Hours(hour+":00"+timePeriod);
			}else{
				hour24 = Utils.convertTo24Hours(hour+timePeriod);
			}
			return hour24;
		}
		String[] hour24 = new String[2];
		String[] startEnd = hour.split(timeSeparator);
		for (int i=0; i<startEnd.length; i++){
			if (startEnd[i].contains(":")){
				if (startEnd[i].length() == 4){
					hour24[i] = "0"+startEnd[i];
				}else{
					hour24[i] = startEnd[i];
				}
			}else{
				//System.out.println("|"+startEnd[i] +"|");
				if (startEnd[i].length() == 1){
					hour24[i] = "0"+startEnd[i]+":00";
				}else{
					hour24[i] = startEnd[i]+":00";
				}
			}
			hour24[i] = Utils.convertTo24Hours(hour24[i]+timePeriod);
		}
		return hour24[0]+"-"+hour24[1];
	}

	/**
	 * Normalizes a date from the I Heart Berlin and Berlin Art Parasites format: "July 13 2013",
	 * the White Trash format: "13 July 2013" 
	 * or the Index format: "13 Jul 2013" to the app's format "13/07/2013"
	 * 
	 * @param inputDate the date in the I Heart Berlin, Berlin Art Parasites, White Trash or Index format
	 * @return a String with the date normalized
	 */
	public static String formatDate(String inputDate){
		String monthNumber;
		String monthLetter = "";
		String day = "";
		String[] monthDayYear = inputDate.split(" ");
		for (int i=0;i<2;i++){ // Just the first two
			// If there is a letter, we have the month
			if (Character.isLetter(monthDayYear[i].charAt(0))){
				monthLetter = monthDayYear[i];
			}else{ // If not, we have the day
				if (monthDayYear[i].length() == 1){
					// We need to add a extra "0"
					day = "0"+monthDayYear[i];
				}else{
					day = monthDayYear[i];
				}
			}
		}
		if (monthLetter.equals("January") || monthLetter.equals("Jan"))
			monthNumber = "01";
		else if (monthLetter.equals("February") || monthLetter.equals("Feb"))
            monthNumber = "02";
		else if (monthLetter.equals("March") || monthLetter.equals("Mar"))
            monthNumber = "03";
        else if (monthLetter.equals("April") || monthLetter.equals("Apr"))
            monthNumber = "04";
        else if (monthLetter.equals("May"))
            monthNumber = "05";
        else if (monthLetter.equals("June") || monthLetter.equals("Jun"))
            monthNumber = "06";
        else if (monthLetter.equals("July") || monthLetter.equals("Jul"))
            monthNumber = "07";
        else if (monthLetter.equals("August") || monthLetter.equals("Aug"))
            monthNumber = "08";
        else if (monthLetter.equals("September") || monthLetter.equals("Sep"))
            monthNumber = "09";
        else if (monthLetter.equals("October") || monthLetter.equals("Oct"))
            monthNumber = "10";
        else if (monthLetter.equals("November") || monthLetter.equals("Nov"))
            monthNumber = "11";
        else if (monthLetter.equals("December") || monthLetter.equals("Dec"))
            monthNumber = "12";
        else
            monthNumber = "00";
		String total = day+"/"+monthNumber+"/"+monthDayYear[2];
		return total.trim(); 
	}
	
	/**
	 * https://gist.github.com/shreeshga/5398506
	 * @param set
	 * @param delim
	 * @return
	 */
	public static String join(Set<String> set, String delim) {
		StringBuilder sb = new StringBuilder();
		String loopDelim = "";
		 
		for (String s : set) {
		sb.append(loopDelim);
		sb.append(s);
		 
		loopDelim = delim;
		}
		 
		return sb.toString();
		}
	
}
