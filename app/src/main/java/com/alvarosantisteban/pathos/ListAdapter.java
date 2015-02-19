package com.alvarosantisteban.pathos;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import com.alvarosantisteban.pathos.R;

/**
 * An adapter for the ExpandableList that enables the population through an ArrayList
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class ListAdapter extends BaseExpandableListAdapter {

	private Context context;
	//private Typeface tf;
	/**
	 * The ArrayList used to get the information 
	 */
	private ArrayList<HeaderInfo> websitesList;
	  
	public ListAdapter(Context context, ArrayList<HeaderInfo> websiteList) {
		 this.context = context;
		 this.websitesList = websiteList;
	}
	  
	/**
	 * Gets the event from the given position within the given group (site)
	 * 
	 * @param groupPosition the position of the group that the event resides in
	 * @param childPosition the position of the event with respect to other events in the group
	 * 
	 * @return the event
	 */
	public Object getChild(int groupPosition, int childPosition) {
		ArrayList<Event> eventsList = websitesList.get(groupPosition).getEventsList();
		return eventsList.get(childPosition);
	}
	 
	/**
	 * Gets the id of the event, which is its position
	 * 
	 * @param groupPosition the position of the group that the event resides in
	 * @param childPosition the position of the event with respect to other events in the group
	 * 
	 * @return the id of the event, which is its position 
	 */
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}
	 
	/**
	 * Gets a View that displays the data for the given event within the given group.
	 * 
	 * @param groupPosition the position of the group that the event resides in
	 * @param childPosition the position of the event with respect to other events in the group
	 * @param isLastChild Whether the event is the last event within the group
	 * @param convertView the old view to reuse, if possible. 
	 * @param parent the parent that this view will eventually be attached to 
	 * 
	 * @return the View corresponding to the event at the specified position 
	 */
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		Event event = (Event) getChild(groupPosition, childPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.child_row, null);
		}
	    
		// Write the information of the event in the way we want 
		//TextView sequence = (TextView) convertView.findViewById(R.id.sequence);
		//sequence.setText(detailInfo.getSequence().trim() + ") ");
		TextView childItem = (TextView) convertView.findViewById(R.id.childItem);
		childItem.setText("- " +Html.fromHtml(event.getName().trim()));
		if (event.isTheEventInteresting()){
	    	childItem.setTextColor(Color.parseColor("#427212"));
	    }else{
	    	childItem.setTextColor(Color.BLACK);
	    }
		return convertView;
	}
	 
	/**
	 * Gets the number of events in a specified group.
	 * 
	 * @param groupPosition the position of the group for which the events count should be returned
	 * 
	 * @return the number of events in the specified group
	 */
	public int getChildrenCount(int groupPosition) {
		ArrayList<Event> eventsList = websitesList.get(groupPosition).getEventsList();
		return eventsList.size();
	}
	 
	/**
	 * Gets the data associated with the given group.
	 * 
	 * @param the position of the group
	 * 
	 * @return the ArrayList of HeaderInfo for the specified group 
	 */
	public Object getGroup(int groupPosition) {
		return websitesList.get(groupPosition);
	}
	 
	/**
	 * Gets the number of groups
	 * 
	 * @return the number of groups
	 */
	public int getGroupCount() {
		return websitesList.size();
	}
	 
	/**
	 * Gets the ID for the group at the given position. The ID is the position itself.
	 * 
	 * @param groupPosition the position of the group for which the ID is wanted
	 * 
	 * @return the ID associated with the group. It is the position itself.
	 */
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	 
	/**
	 * Gets a View that displays the given group. 
	 * 
	 * @param groupPosition the position of the group for which the View is returned
	 * @param isExpanded whether the group is expanded or collapsed
	 * @param convertView the old view to reuse, if possible. 
	 * @param parent the parent that this view will eventually be attached to
	 * 
	 * @return the View corresponding to the group at the specified position 
	 */
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {	
		HeaderInfo headerInfo = (HeaderInfo) getGroup(groupPosition);
		// I had to comment it, because it creates problems with the views
		//if (convertView == null) {
			LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inf.inflate(R.layout.group_heading, null);
		//}
			   
		TextView heading = (TextView) convertView.findViewById(R.id.heading);
		String singPl = " events";
		if (headerInfo.getEventsNumber() == 1){
			singPl =" event";
		}
		heading.setText(getGroupName(headerInfo.getName().trim()) +" - " +headerInfo.getEventsNumber() +singPl);
		// If there are no events, make the header's color gray
		if(headerInfo.getEventsNumber() == 0){
			heading.setTextColor(Color.GRAY);
		}
		//tf = Typeface.createFromAsset(context.getAssets(), "font/Berylium.ttf");
		//heading.setTypeface(tf);
		return convertView;
	}
	
	/**
	 * Returns the name of header group that will be printed out
	 * 
	 * @param headerName the header name to be changed
	 * @return the name for that header group to be printed
	 */
	private String getGroupName(String headerName){
		String sentence = "Error";
		if(headerName.equals("Exhibition")){
			sentence = "Exhibitions";
		}else if(headerName.equals("Talk")){
			sentence = "Talks";
		}else if(headerName.equals("Party")){
			sentence = "Parties";
		}else if(headerName.equals("Screening")){
			sentence = "Screenings";
		}else if(headerName.equals("Concert")){
			sentence = "Concerts";
		}else if(headerName.equals("Other")){
			sentence = "Not categorized";
		}else{
			sentence = headerName;
		}
		return sentence;
	}
	
	/**
	 * Returns the total number of children in all displayed groups
	 * 
	 * @return the total number of children
	 */
	public int getTotalChildrenCount(){
		int total = 0;
		for(int i=0; i<getGroupCount(); i++){
			total += getChildrenCount(i);
		}
		return total;
	}
	 
	/**
	 * Indicates whether the child and group IDs are stable across changes to the underlying data.
	 * 
	 * @return always true
	 */
	public boolean hasStableIds() {
		return true;
	}
	 
	/**
	 * Whether the child at the specified position is selectable.
	 * 
	 * @return always true
	 */
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
