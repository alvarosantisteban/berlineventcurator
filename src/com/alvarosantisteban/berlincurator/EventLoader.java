package com.alvarosantisteban.berlincurator;

import java.util.List;

import android.content.Context;

public interface EventLoader {
	
	List<Event> load(Context context);
}
