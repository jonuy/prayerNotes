package com.jdroyd.prayerNotes;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * Will be responsible for interfacing with AlarmManager service
 * 
 * set alarm, set time for it, repeating reminders, displaying UI dialogs
 */
public class PNAlarmSetter extends Dialog implements OnClickListener {
	// Logging tag
	private static final String TAG = "PNAlarmSetter";
	
	private static final String AM = "AM";
	private static final String PM = "PM";
	
	private Context mContext;
	
	// Date and Time strings to display and return to Activity
	private String mTime;
	private String mDate;
	
	// 
	private TextView mDateDisplay;
	private TextView mTimeDisplay;
	
	//
	private int mYear;
	private int mMonth;
	private int mDay;
	
	//
	private int mHour;
	private int mMinute;
	private int mAM_PM;
	
	// last set date and time of alarm in milliseconds
	private long mAlarmTime;
	
	
	/**
	 * Constructor
	 */
	public PNAlarmSetter(Context context) {
		super(context);
		
		mContext = context;
	}
	
	public void initView() {
		
		setContentView(R.layout.alarm_main);
		setTitle(R.string.dialog_alarm_title);
		
		// Initially display current time plus 5 minutes
		mTimeDisplay = (TextView)findViewById(R.id.alarm_main_time);
		mDateDisplay = (TextView)findViewById(R.id.alarm_main_date);
		
		Calendar cal = Calendar.getInstance();
		// Adding 5 minutes to the current time
		cal.add(Calendar.MINUTE, 5);
		
		mHour = cal.get(Calendar.HOUR);
		mMinute = cal.get(Calendar.MINUTE);
		mAM_PM = cal.get(Calendar.AM_PM);
		Log.v(TAG, mHour+":"+mMinute+" "+mAM_PM);
		
		mMonth = cal.get(Calendar.MONTH);
		mDay = cal.get(Calendar.DATE);
		mYear = cal.get(Calendar.YEAR);
		
		updateDisplay();
		
		// Setup onClickListeners
		Button setDate = (Button)findViewById(R.id.alarm_main_date_button);
		Button setTime = (Button)findViewById(R.id.alarm_main_time_button);
		Button okButton = (Button)findViewById(R.id.alarm_main_ok_button);
		
		setDate.setOnClickListener(this);
		setTime.setOnClickListener(this);
		okButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch( id ) {
		case R.id.alarm_main_date_button:
			createDateDialog();
			break;
		case R.id.alarm_main_time_button:
			createTimeDialog();
			break;
		case R.id.alarm_main_ok_button:
			// Update mAlarmTime with current date and time
			Calendar cal = Calendar.getInstance();
			
			int hourOfDay = getHourOfDay(mHour, mAM_PM);
			cal.set(mYear, mMonth, mDay, hourOfDay, mMinute);
			cal.set(Calendar.SECOND, 0);
			mAlarmTime = cal.getTimeInMillis();
			
			dismiss();
			break;
		}
	}
	
	/**
	 * Converts the millisecond time to hour:minute am/pm string
	 * @param timeMS time in milliseconds
	 * @return hour:minute am/pm string
	 */
	public String getTimeFromMillis(long timeMS) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeMS);
		int hour = cal.get(Calendar.HOUR);
		int minute = cal.get(Calendar.MINUTE);
		int am_pm = cal.get(Calendar.AM_PM);
		
		return getTimeString(hour, minute, am_pm);
	}
	
	/**
	 * @param hour range from 0-11
	 * @param minute range from 0-59
	 * @param am_pm either Calendar.AM or .PM
	 * @return string representation of the passed in values - 00:00 AM/PM
	 */
	private String getTimeString(int hour, int minute, int am_pm) {
		if( hour == 0 )
			hour = 12;
		
		return	pad(hour) + ":" + 
				pad(minute) + " " + 
				((am_pm == Calendar.AM) ? AM : PM);
	}
	
	//
	public String getDateFromMillis(long timeMS) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeMS);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DATE);
		int year = cal.get(Calendar.YEAR);
		
		return getDateString(month, day, year);
	}
	
	/**
	 * @param month range from 0-11
	 * @param day range from 1-31
	 * @param year
	 * @return string representation of the passed in values - ##/##/20##
	 */
	private String getDateString(int month, int day, int year) {
		// Month is 0 based, so add 1
		return	(month+1) + "/" + 
				day + "/" + 
				year;
	}
	
	/**
	 * Converts AM/PM hour value to "hour of day" value
	 * @param hour AM/PM hour between 0-11
	 * @param am_pm either Calendar.AM or .PM
	 * @return "hour of day" equivalent - between 0-23
	 */
	private int getHourOfDay(int hour, int am_pm) {
		return hour + (am_pm == Calendar.AM ? 0 : 12);
	}
	
	/**
	 * Accessor to get long with the set alarm time
	 */
	public long getAlarmTime() {
		return mAlarmTime;
	}
	
	/**
	 * Prefixes number with a 0.  Specifically for displaying time.
	 */
	private static String pad(int c) {
		if( c < 10 )
			return "0" + String.valueOf(c);
		else
			return String.valueOf(c);
	}
	
	/**
	 * Updates the time and date strings and the TextViews they're displayed in
	 */
	private void updateDisplay() {
		mTime = getTimeString(mHour, mMinute, mAM_PM);
		mDate = getDateString(mMonth, mDay, mYear);
		
		mTimeDisplay.setText(mTime);
		mDateDisplay.setText(mDate);
	}
	
	/**
	 * Callback received when user sets the date in the dialog
	 */
	private DatePickerDialog.OnDateSetListener mDateSetListener =
		new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				mYear = year;
				mMonth = monthOfYear;
				mDay = dayOfMonth;
				
				updateDisplay();
			}
		};
	
	/**
	 * Creates the DatePickerDialog and shows it
	 */
	private void createDateDialog() {
		DatePickerDialog datePicker = new DatePickerDialog(mContext,
				mDateSetListener, mYear, mMonth, mDay);
		datePicker.show();
	}
	
	/**
	 * Callback received when user sets the time in the dialog
	 */
	private TimePickerDialog.OnTimeSetListener mTimeSetListener = 
		new TimePickerDialog.OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				Log.v(TAG, "time set: "+hourOfDay+":"+minute);
				if( hourOfDay >= 12 ) {
					mHour = hourOfDay - 12;
					mAM_PM = Calendar.PM;
				}
				else {
					mHour = hourOfDay;
					mAM_PM = Calendar.AM;
				}
				
				mMinute = minute;
				
				updateDisplay();
			}
		};

	/**
	 * Creates the TimePickerDialog and shows it
	 */
	private void createTimeDialog() {
		int hourOfDay = getHourOfDay(mHour, mAM_PM);
		TimePickerDialog timePicker = new TimePickerDialog(mContext,
				mTimeSetListener, hourOfDay, mMinute, false);
		timePicker.show();
	}
	
	/**
	 * Setup the event using AlarmManager
	 */
	public void scheduleAlarm(long time, Long position, String contentText) {
		if( time > 0 && position != null && contentText != null ) {
			Intent intent = new Intent(mContext, PNAlarmReceiver.class);
			// set Uri data to distinguish between intents for different notes 
			intent.setData(Uri.parse("note_id:"+position));
			
			intent.putExtra(PNDbAdapter.PNKEY_ROWID, position);
			//TODO: get "content_text" statically defined somewhere
			intent.putExtra("content_text", contentText);
			// TODO: use static var instead of 102984 for request code
			PendingIntent sender = PendingIntent.getBroadcast(mContext, 102984, 
					intent, PendingIntent.FLAG_UPDATE_CURRENT /*FLAG_CANCEL_CURRENT*//* FLAG_UPDATE_CURRENT*/);
			
			AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, time, sender);
			
			// Debug text
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(time);
			Log.v(TAG, "SETTING ALARM FOR: note="+position+" @ time="+cal.getTime().toString());
		}
	}
	
	/**
	 * Cancel alarm
	 */
	public void cancelAlarm(Long position) {
		// Recreate the PendingIntent to cancel the alarm
		if( position != null ) {
			Intent intent = new Intent(mContext, PNAlarmReceiver.class);
			// set Uri data to distinguish between intents for different notes 
			intent.setData(Uri.parse("note_id:"+position));
			
			intent.putExtra(PNDbAdapter.PNKEY_ROWID, position);
			
			PendingIntent sender = PendingIntent.getBroadcast(mContext, 102984, 
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
			am.cancel(sender);
			
			Log.v(TAG, "CANCELING ALARM FOR: note="+position);
		}
	}
}