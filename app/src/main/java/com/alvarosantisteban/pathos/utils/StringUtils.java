package com.alvarosantisteban.pathos.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * Methods that are used across the application that deal with strings
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public final class StringUtils {
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "StringUtils";
	
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
		} catch (Exception e){
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 *  A small separate function to extract the hour (based on am/pm) from the html mess.
	 *  
	 * @param theTime the String with the time
	 * @return the time in the format HH:MM or HH:MM-HH:MM or an empty string if there was no time or a problem arose.
	 */
	public static String extractTime(String theTime) {
		try{
			//Log.d(TAG,"|"+theTime +"|");
			theTime = theTime.replace(".", ":");
			//Log.d(TAG,"|"+theTime +"|");
			String timePeriod = "";
			// Make sure that there is a time
			if (theTime.contains("pm")){
				timePeriod = "pm";
			}else if (theTime.contains("am")){
				timePeriod = "am";
			}else{
				// Is not a time
				Log.w(TAG,"It's not a time. We return an empty string");
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
			}else if (hour.contains("�")){
				timeSeparator = "�";
			}else{	
				String hour24;
				if (hour.length() == 1){
					hour24 = StringUtils.convertTo24Hours("0"+hour+":00"+timePeriod);
				}else if(hour.length() == 4){
					hour24 = StringUtils.convertTo24Hours("0"+hour+timePeriod);
				}else if(hour.length() == 2){
					hour24 = StringUtils.convertTo24Hours(hour+":00"+timePeriod);
				}else{
					hour24 = StringUtils.convertTo24Hours(hour+timePeriod);
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
				hour24[i] = StringUtils.convertTo24Hours(hour24[i]+timePeriod);
			}
			return hour24[0]+"-"+hour24[1];
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
		
	}

	/**
	 * Normalizes a date from the I Heart Berlin and Berlin Art Parasites format: "July 13 2013",
	 * the White Trash format: "13 July 2013" 
	 * or the Index format: "13 Jul 2013" to the app's format "13/07/2013"
     *
     * It can handle lower and upper cases but there needs to be a separation between the month/day/year
	 * 
	 * @param inputDate the date in the I Heart Berlin, Berlin Art Parasites, White Trash or Index format
	 * @return a String with the date normalized
	 */
	public static String formatDate(String inputDate){
        String total;
        try {
            String monthNumber;
            String monthLetter = "";
            String day = "";
            String[] monthDayYear = inputDate.split(" ");
            for (int i = 0; i < 2; i++) { // Just the first two
                // If there is a letter, we have the month
                if (Character.isLetter(monthDayYear[i].charAt(0))) {
                    monthLetter = monthDayYear[i];
                } else { // If not, we have the day
                    if (monthDayYear[i].length() == 1) {
                        // We need to add a extra "0"
                        day = "0" + monthDayYear[i];
                    } else {
                        day = monthDayYear[i];
                    }
                }
            }
            monthLetter = monthLetter.toLowerCase();
            if (monthLetter.equals("january") || monthLetter.equals("jan"))
                monthNumber = "01";
            else if (monthLetter.equals("february") || monthLetter.equals("feb"))
                monthNumber = "02";
            else if (monthLetter.equals("march") || monthLetter.equals("mar"))
                monthNumber = "03";
            else if (monthLetter.equals("april") || monthLetter.equals("apr"))
                monthNumber = "04";
            else if (monthLetter.equals("may"))
                monthNumber = "05";
            else if (monthLetter.equals("june") || monthLetter.equals("jun"))
                monthNumber = "06";
            else if (monthLetter.equals("july") || monthLetter.equals("jul"))
                monthNumber = "07";
            else if (monthLetter.equals("august") || monthLetter.equals("aug"))
                monthNumber = "08";
            else if (monthLetter.equals("september") || monthLetter.equals("sep"))
                monthNumber = "09";
            else if (monthLetter.equals("october") || monthLetter.equals("oct"))
                monthNumber = "10";
            else if (monthLetter.equals("november") || monthLetter.equals("nov"))
                monthNumber = "11";
            else if (monthLetter.equals("december") || monthLetter.equals("dec"))
                monthNumber = "12";
            else
                monthNumber = "00";
            total = day + "/" + monthNumber + "/" + monthDayYear[2];
        }catch(Exception e){
            Log.e(TAG, "Exception formatting the date. Return an empty string as date.");
            e.printStackTrace();
            total = "";
        }
		return total.trim(); 
	}
	
	/**
	 * https://gist.github.com/shreeshga/5398506
	 * @param set the set of strings that want to be joined
	 * @param delim the delimiter used to join the different strings
	 * @return null if one of the parameters is null, a String with all the Strings inside the set joined using the delimiter as "glue"
	 */
	public static String join(Set<String> set, String delim) {
		if(set != null && delim != null){
			StringBuilder sb = new StringBuilder();
			String loopDelim = "";
			 
			for (String s : set) {
				sb.append(loopDelim);
				sb.append(s);
				 
				loopDelim = delim;
			}
			 
			return sb.toString();
		}
		return null;
	}
	
	/**
	 * Capitalizes the first character of each word.
	 * Taken from: http://stackoverflow.com/questions/1892765/capitalize-first-char-of-each-word-in-a-string-java
	 * 
	 * @param string the string to be capitalized
	 * @return the string once capitalized
	 */
	public static String capitalizeString(String string) {
		char[] chars = string.toLowerCase(Locale.GERMAN).toCharArray();
		boolean found = false;
		for (int i = 0; i < chars.length; i++) {
			if (!found && Character.isLetter(chars[i])) {
				chars[i] = Character.toUpperCase(chars[i]);
				found = true;
		    } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
		    	found = false;
		    }
		}
		return String.valueOf(chars);
	}
}
