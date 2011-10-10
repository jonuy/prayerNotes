package com.jdroyd.prayerNotes;

import java.util.Calendar;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Will be responsible for interfacing with AlarmManager service
 * 
 * set alarm, set time for it, repeating reminders, displaying UI dialogs
 */
public class PNAlarmSetter extends Dialog implements OnClickListener {
	
	private Context mContext;
	
	// Date and Time strings to display and return to Activity
	private String mTime;
	private String mDate;
	
	/**
	 * Constructor
	 */
	public PNAlarmSetter(Context context) {
		super(context);
		
		mContext = context;
		initView();
	}
	
	private void initView() {
		
		setContentView(R.layout.alarm_main);
		setTitle(R.string.dialog_alarm_title);
		
		// Initially display current time plus 5 minutes
		TextView timeText = (TextView)findViewById(R.id.alarm_main_time);
		TextView dateText = (TextView)findViewById(R.id.alarm_main_date);
		
		Calendar cal = Calendar.getInstance();
		// Adding 5 minutes to the current time
		cal.add(Calendar.MINUTE, 5);
		
		int hour = cal.get(Calendar.HOUR);
		int min = cal.get(Calendar.MINUTE);
		int am_pm = cal.get(Calendar.AM_PM);
		
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DATE);
		int year = cal.get(Calendar.YEAR);

		mTime = hour + ":" + min + " " + ((am_pm == Calendar.AM) ? "AM" : "PM");
		mDate = month + "/" + day + "/" + year;
		timeText.setText(mTime);
		dateText.setText(mDate);
		
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
			break;
		case R.id.alarm_main_time_button:
			break;
		case R.id.alarm_main_ok_button:
			dismiss();
			break;
		}
	}
	
	public String getTime() {
		return mTime;
	}
	
	public String getDate() {
		return mDate;
	}
	
	public void scheduleAlarm() {
		
	}
}
