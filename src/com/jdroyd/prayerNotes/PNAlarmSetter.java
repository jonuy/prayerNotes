package com.jdroyd.prayerNotes;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
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
		mTimeDisplay = (TextView)findViewById(R.id.alarm_main_time);
		mDateDisplay = (TextView)findViewById(R.id.alarm_main_date);
		
		Calendar cal = Calendar.getInstance();
		// Adding 5 minutes to the current time
		cal.add(Calendar.MINUTE, 5);
		
		mHour = cal.get(Calendar.HOUR);
		mMinute = cal.get(Calendar.MINUTE);
		mAM_PM = cal.get(Calendar.AM_PM);
		
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
			dismiss();
			break;
		}
	}
	
	/**
	 * Accessor to return Time string
	 */
	public String getTime() {
		return mTime;
	}
	
	/**
	 * Accessor to return Date string
	 */
	public String getDate() {
		return mDate;
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
		mTime = pad(mHour) + ":" + 
				pad(mMinute) + " " + 
				((mAM_PM == Calendar.AM) ? AM : PM);
		// Month is 0 based, so add 1
		mDate = (mMonth+1) + "/" + 
				mDay + "/" + 
				mYear;
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
				mHour = hourOfDay;
				if( mHour > 12 ) {
					mHour -= 12;
					mAM_PM = Calendar.PM;
				}
				else {
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
		TimePickerDialog timePicker = new TimePickerDialog(mContext,
				mTimeSetListener, mHour, mMinute, false);
		timePicker.show();
	}
	
	public void scheduleAlarm() {
		
	}
}