package com.jdroyd.prayerNotes;

import java.util.Calendar;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

/**
 * Will be responsible for interfacing with AlarmManager service
 * 
 * set alarm, set time for it, repeating reminders, displaying UI dialogs
 */
public class PNAlarmManager {
	// Private instance and constructor to enforce singleton
	private static final PNAlarmManager INSTANCE = new PNAlarmManager();
	private PNAlarmManager() {
	}
	public static PNAlarmManager getInstance() { return INSTANCE; }
	
	public void scheduleAlarm() {
		
	}
	
	public Dialog createDialog(Context context) {
		Dialog dialog = new Dialog(context);
		
		dialog.setContentView(R.layout.alarm_main);
		dialog.setTitle(R.string.dialog_alarm_title);
		
		// Initially display current time plus 5 minutes
		TextView timeText = (TextView)dialog.findViewById(R.id.alarm_main_time);
		TextView dateText = (TextView)dialog.findViewById(R.id.alarm_main_date);
		
		Calendar cal = Calendar.getInstance();
		// Adding 5 minutes to the current time
		cal.add(Calendar.MINUTE, 5);
		
		int hour = cal.get(Calendar.HOUR);
		int min = cal.get(Calendar.MINUTE);
		int am_pm = cal.get(Calendar.AM_PM);
		
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DATE);
		int year = cal.get(Calendar.YEAR);

		timeText.setText(hour + ":" + min + ((am_pm == Calendar.AM) ? "AM" : "PM"));
		dateText.setText(month + "/" + day + "/" + year);
		
		return dialog;
	}
}
