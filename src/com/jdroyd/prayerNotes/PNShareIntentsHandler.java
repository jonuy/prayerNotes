package com.jdroyd.prayerNotes;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

public class PNShareIntentsHandler {
	
	// Intents to handle
	public static enum PNIntent {
		EMAIL,
		GMAIL
	};
	
	/**
	 * Private instance of this class
	 */
	private static final PNShareIntentsHandler INSTANCE = new PNShareIntentsHandler();
	
	/**
	 * Private constructor
	 */
	private PNShareIntentsHandler() {
	}
	
	/**
	 * Public method to access this singleton
	 */
	public static final PNShareIntentsHandler getInstance() {return INSTANCE;}
	
	public Intent getIntent(PNIntent id) {
		Intent intent = null;
		
		switch( id ) {
		case EMAIL:
			intent = new Intent(android.content.Intent.ACTION_SEND);
	        intent.setType("text/plain");
			break;
		case GMAIL:
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivity");
			break;
		}
		
		return intent;
	}
	
	public boolean isActivityAvailable(Context context, PNIntent id) {
		Intent intent = getIntent(id);
		List<ResolveInfo> list = context.getPackageManager()
			.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		
		// If the list returns any, then Activity is available
		return list.size() > 0;
	}
	
	/**
	 * Handles what to do when we catch an ActivityNotFoundException
	 */
	public void onActivityNotFoundError(Context context) {
        Toast.makeText(context, R.string.error_activity_not_found, Toast.LENGTH_LONG);
	}
}
