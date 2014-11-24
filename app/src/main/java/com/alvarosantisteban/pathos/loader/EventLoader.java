package com.alvarosantisteban.pathos.loader;

import java.util.List;

import com.alvarosantisteban.pathos.Event;

import android.content.Context;

public interface EventLoader {
	
	List<Event> load(Context context);

}
