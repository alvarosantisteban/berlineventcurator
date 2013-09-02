package com.alvarosantisteban.berlincurator.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class CustomListPreference extends ListPreference {

	public CustomListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		System.out.println("CustomListPreference");
	}
	
	public CustomListPreference(Context context) {
        super(context);
    }

	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder){
		System.out.println("onPrepareDialogBuilder");
		builder.setTitle(getTitle());
		builder.setMessage(getDialogMessage());
        super.onPrepareDialogBuilder(builder);
	}
}
