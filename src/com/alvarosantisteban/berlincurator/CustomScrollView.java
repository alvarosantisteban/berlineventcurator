package com.alvarosantisteban.berlincurator;

import com.google.android.gms.maps.MapView;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class CustomScrollView extends ScrollView{
	
	MapView map;
	
	
	public CustomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setMap(MapView theMap) {
		System.out.println("setMap");
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
	    		System.out.println("El rectangulo del mapa contiene el punto tocado");
	    		return false;
	    	}
		}
		return super.onInterceptTouchEvent(ev);
	}

	@SuppressWarnings("unused")
	private void printSeveralCoordinates(MotionEvent ev, Rect bound) {
		System.out.println("mapita X:"+map.getX() +"mapita Y:"+map.getY());
		System.out.println("coordenadas mapita:"+bound.top);
		System.out.println("ev.getX(): "+ev.getX() +" / ev.getY(): "+ev.getY());
		System.out.println("getScrollY (): "+getScrollY());
	}	
}