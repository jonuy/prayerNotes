package com.jdroyd.prayerNotes;

public class Constants {
	// private constructor.  prevent instantiation
	private Constants() {
		throw new AssertionError();
	}
	
	// identifier for activity actions
	public static final int ACTIVITY_CREATE = 0;
	public static final int ACTIVITY_EDIT = 1;
	public static final int ACTIVITY_IMG_GALLERY = 2;
	public static final int ACTIVITY_SHARE = 3;
	
	// identifier for context actions
	public static final int CONTEXT_DELETE_ID = 0;
	public static final int CONTEXT_PRAYED_ID = 1;
	public static final int CONTEXT_OPEN_ID = 2;
	
	// identifier for dialog actions
	public static final int DIALOG_DELETE_NOTE = 0;
	public static final int DIALOG_CANCEL_EDIT = 1;
	public static final int DIALOG_SET_ALARM = 2;
	
	// identifier for menu actions
	public static final int MENU_CANCEL_ID = 0;
}
