package com.alvarosantisteban.berlincurator;

import com.google.android.gms.maps.MapView;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Custom scroll view to avoid scrolling problems with the google map of the EventActivity.
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class CustomScrollView extends ScrollView{
	
	/**
	 * Used for logging purposes
	 */
	private static final String TAG = "CustomScrollView";
	
	MapView map;
	
	public CustomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setMap(MapView theMap) {
		Log.v(TAG,"setMap");
		map = theMap;
	}

	/**
	 * Intercepts touch event so the area of the mapview can be controlled by itself.
	 * 
	 * @return false if the event is passed to the next bottom view and not handled.
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev){
		if(map != null){
			Rect bound = new Rect();
	    	map.getHitRect(bound);
			//printSeveralCoordinates(ev, bound);
	    	if(bound.contains(Math.round(ev.getX()), Math.round(ev.getY())+getScrollY())){
	    		return false;
	    	}
		}
		return super.onInterceptTouchEvent(ev);
	}

	@SuppressWarnings("unused")
	private void printSeveralCoordinates(MotionEvent ev, Rect bound) {		
		Log.v(TAG,"mapita X:"+map.getX() +"mapita Y:"+map.getY());
		Log.v(TAG,"coordenadas mapita:"+bound.top);
		Log.v(TAG,"ev.getX(): "+ev.getX() +" / ev.getY(): "+ev.getY());
		Log.v(TAG,"getScrollY (): "+getScrollY());
	}	
}