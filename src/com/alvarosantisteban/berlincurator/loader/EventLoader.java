package com.alvarosantisteban.berlincurator.loader;

import java.util.List;

import com.alvarosantisteban.berlincurator.Event;

import android.content.Context;

public interface EventLoader {
	
	List<Event> load(Context context);

}
