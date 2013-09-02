package com.alvarosantisteban.berlincurator;

import java.io.Serializable;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * The information of the event. Implements Serializable to allow this objects to be passed as extras in an Intent 
 * 
 * Make it faster:
 * http://stackoverflow.com/questions/2139134/how-to-send-an-object-from-one-android-activity-to-another-using-intents
 * http://stackoverflow.com/questions/2736389/how-to-pass-object-from-one-activity-to-another-in-android
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
@DatabaseTable(tableName = "events")
public class Event implements Serializable{
	
	public Event(){
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The unique ID from the event, which is randomly generated
	 */
	@DatabaseField(generatedId = true, useGetSet = true)
	private int id;
	
	/**
	 * The position of the event inside the list
	 */
	@DatabaseField(useGetSet = true)
	private String sequence = "";
	/**
	 * The name of the event
	 */
	@DatabaseField(canBeNull = false, useGetSet = true)
	private String name = "";
	
	/**
	 * The day when the event will take place
	 */
	@DatabaseField(canBeNull = false, useGetSet = true)
	private String day = "";
	
	/**
	 * The time when the event begins
	 */
	@DatabaseField(useGetSet = true)
	private String hour = "";
	
	/**
	 * The description of the event
	 */
	@DatabaseField(useGetSet = true)
	private String description = "";
	
	/**
	 * The html code corresponding to a image stored in the web
	 */
	private String image="";
	
	/**
	 * The location of the event (if known)
	 */
	@DatabaseField(useGetSet = true)
	private String location = "";
	
	public static final String LINK_NAME = "links"; 
	/**
	 * The links with more information
	 */
	@DatabaseField(dataType = DataType.SERIALIZABLE, useGetSet = true, columnName = LINK_NAME)
	private String[] links = {"",""};
	
	/**
	 * Tells if the user marked the event as "interesting"
	 */
	@DatabaseField(useGetSet = true)
	private boolean isInteresting = false;
	
	/**
	 * Tells if the description of the event is in german
	 */
	@DatabaseField(useGetSet = true)
	private boolean isDescriptionInGerman = false;
	
	/**
	 * The name of the website where the event comes from
	 */
	@DatabaseField(useGetSet = true)
	private String eventsOrigin = "";
	
	/**
	 * The url of the website where the event comes from
	 */
	@DatabaseField(useGetSet = true)
	private String originsWebsite = "";
	
	/**
	 * The tag for the thema of this event
	 */
	@DatabaseField(useGetSet = true)
	private String themaTag = "";
	
	/**
	 * The tag for the type of event
	 */
	@DatabaseField(useGetSet = true)
	private String typeTag = "";
	
	//////////////////////
	// GET AND SETS
	//////////////////////
	
	public int getId(){
		return this.id;
	}
	
	public void setId(int id){
		//Does not do anything
		this.id = id;
	}
	
	/**
	 * Returns the position of the event inside its list
	 * @return
	 */
	public String getSequence() {
		return sequence;
	}
	 
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	 
	public String getName() {
		 return name;
	}
	 
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLink(){
		return links[0];
	}
	
	public String[] getLinks(){
		return links;
	}
	
	public String getLocation(){
		return location;
	}
	
	public boolean isTheEventInteresting(){
		return this.isInteresting;
	}
	
	/**
	 * Same method as isTheEventInteresting made for ORMLite
	 * @return
	 */
	public boolean getIsInteresting(){
		return this.isInteresting;
	}
	
	public void markEventAsInteresting(boolean isThisInteresting){
		this.isInteresting = isThisInteresting;
	}
	
	/**
	 * Same method as markEventAsInteresting made for ORMLite
	 * @param isThisInteresting
	 */
	public void setIsInteresting(boolean isThisInteresting){
		this.isInteresting = isThisInteresting;
	}
	
	public boolean getIsDescriptionInGerman(){
		return this.isDescriptionInGerman;
	}
	
	public void setIsDescriptionInGerman(boolean isInGerman){
		this.isDescriptionInGerman = isInGerman;
	}
	
	public void setLocation(String theLocation){
		this.location = theLocation;
	}
	
	/**
	 * Set the two links of the event.
	 * @param links the Strings containing the links of the event
	 */
	public void setLinks(String[] links){
		for (int i=0; i<links.length && i<2; i++){
			this.links[i] = links[i];
		}
	}
	
	/**
	 * Sets a link for the event.
	 * @param link the String containing one link of the event
	 */
	public void setLink(String link){
		if (this.links[0] == ""){
			this.links[0] = link;
		}else{
			this.links[1] = link;
		}
	}
	
	public String getDay(){
		return day;
	}
	
	public void setDay(String day){
		this.day = day;
	}
	
	public String getHour(){
		return hour;
	}
	
	public void setHour(String hour){
		this.hour = hour;
	}

	public void setDescription(String description){
		this.description = description;
	}
	
	public String getDescription(){
		return description;
	}
	
	public String getImage(){
		return image;
	}
	
	public void setImage(String image){
		this.image = image;
	}
	
	public String getEventsOrigin(){
		return this.eventsOrigin;
	}
	
	public void setEventsOrigin(String websiteName){
		this.eventsOrigin = websiteName;
	}
	
	public String getThemaTag(){
		return this.themaTag;
	}
	
	public void setThemaTag(String newThemaTag){
		this.themaTag = newThemaTag;
	}
	
	public String getTypeTag(){
		return this.typeTag;
	}
	
	public void setTypeTag(String newTypeTag){
		this.typeTag = newTypeTag;
	}
	
	public String getOriginsWebsite() {
		return this.originsWebsite;
	}

	public void setOriginsWebsite(String theOriginsWebsite) {
		this.originsWebsite = theOriginsWebsite;
	}
	
	/*
	public void print() {
		System.out.println("Name:"+getName());
		System.out.println("Day:"+getDay());
		printLinks();	
	}
	
	public void printLinks(){
		for (int i=0; i<links.length;i++){
			System.out.println("Link["+i +"]:"+links[i]);
		}
	}
	*/
}
