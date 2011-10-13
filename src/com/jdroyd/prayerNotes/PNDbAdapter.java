/**
 * TODO: move some of the public static final vars into their own
 *   constants class?
 */

package com.jdroyd.prayerNotes;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PNDbAdapter {

	// Database table name
	private static final String PN_DATABASE_TABLE_NAME = "prayerNotesDb";
	
	/**
	 * Table columns
	 */
	// Internal row id used as primary key
	public static final String PNKEY_ROWID = "_id";
	private static final String PNKEY_ROWID_TYPE = "INTEGER";
	private static final String PNKEY_ROWID_ARGS = "PRIMARY KEY AUTOINCREMENT";
	
	// Text for the note
	public static final String PNKEY_NOTE_TEXT = "NoteText";
	private static final String PNKEY_NOTE_TEXT_TYPE = "TEXT";
	private static final String PNKEY_NOTE_TEXT_ARGS = "NOT NULL";
	
	// File path for optional image attached to note
	public static final String PNKEY_NOTE_IMG = "NoteImage";
	private static final String PNKEY_NOTE_IMG_TYPE = "TEXT";
	private static final String PNKEY_NOTE_IMG_ARGS = "";
	
	// Date note was created, packed into an int
	// (month) << 24 | (day) << 16 | (year)
	// ex: 10/29/1984 = 10 << 24 | 29 << 16 | 1984 = 169674688
	public static final String PNKEY_DATE_CREATED = "DateCreated";
	private static final String PNKEY_DATE_CREATED_TYPE = "INTEGER";
	private static final String PNKEY_DATE_CREATED_ARGS = "NOT NULL";
	
	// Date note was last marked as prayed for.  Packed into int in the same way
	// PNKEY_DATE_CREATED is.  Set to 0 if not prayed for yet.
	public static final String PNKEY_LAST_PRAYED = "DateLastPrayed";
	private static final String PNKEY_LAST_PRAYED_TYPE = "INTEGER";
	private static final String PNKEY_LAST_PRAYED_ARGS = "";
	
	// Time in milliseconds that alarm is set for
	public static final String PNKEY_ALARM = "Alarm";
	private static final String PNKEY_ALARM_TYPE = "INTEGER";
	private static final String PNKEY_ALARM_ARGS = "";
	
	/**
	 * Database creation sql statement
	 */
	private static final String PN_DATABASE_CREATE =
		"CREATE TABLE " + PN_DATABASE_TABLE_NAME + "("
		+ PNKEY_ROWID + " " + PNKEY_ROWID_TYPE + " " + PNKEY_ROWID_ARGS + ", "
		+ PNKEY_NOTE_TEXT + " " + PNKEY_NOTE_TEXT_TYPE + " " + PNKEY_NOTE_TEXT_ARGS + ", "
		+ PNKEY_NOTE_IMG + " " + PNKEY_NOTE_IMG_TYPE + " " + PNKEY_NOTE_IMG_ARGS + ", "
		+ PNKEY_DATE_CREATED + " " + PNKEY_DATE_CREATED_TYPE + " " + PNKEY_DATE_CREATED_ARGS + ", "
		+ PNKEY_LAST_PRAYED + " " + PNKEY_LAST_PRAYED_TYPE + " " + PNKEY_LAST_PRAYED_ARGS + ", "
		+ PNKEY_ALARM + " " + PNKEY_ALARM_TYPE + " " + PNKEY_ALARM_ARGS
		+ ");";
	
	// Name of the database file
	public static final String PN_DATABASE_FILE_NAME = "prayerNotesDb";
	
	// Database version number
	// onUpgrade() used if Db version is upgraded
	// ChangeLog:
	//	v2 = original
	//	v3 = adding alarm time
	private static final int PN_DATABASE_VERSION = 3;
	
	// Database helper
	private PNDatabaseHelper mDbHelper;
	
	// Database object
	private SQLiteDatabase mDb;
	
	// Context for database to work in
	private Context mContext;
	
	/**
	 * Constructor - takes the context to allow the database to be opened/created
	 * 
	 * @param context the Context within which to work
	 */
	public PNDbAdapter(Context context) {
		this.mContext = context;
	}
	
	/**
	 * 
	 */
	public PNDbAdapter open() throws SQLException {
		Log.v("PNDbAdapter", "open()");
		if( mDbHelper == null) {
			mDbHelper = new PNDatabaseHelper(mContext);
		}
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
	/**
	 * 
	 */
	public void close() {
		Log.v("PNDbAdapter", "close()");
		mDbHelper.close();
	}
	
	/**
	 * Gets current date from Calendar and packs its value into a single int
	 * (month) << 24 | (day) << 16 | (year)
	 * @return packed integer of the current date 
	 */
	public int getCurrentDateForDb() {
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DATE);
		int year = cal.get(Calendar.YEAR);
		int packedDate = month << 24 | day << 16 | year;
		
		return packedDate;
	}
	
	/**
	 * Converts the packed int from the database into readable String
	 * @param packedDate Assumed packed int from database
	 * @return human-readable String
	 */
	public String convertDbDateToString(Integer packedDate) {
		String unpackedDate = null;
		
		if( packedDate != null ) {
			Integer year = packedDate & 0xFFFF;
			Integer day = (packedDate >> 16) & 0xFF;
			Integer month = (packedDate >> 24) & 0xFF;
			
			unpackedDate = getLocalizedMonth(month) + " " + day.toString() + ", " + year.toString();
		}
		
		return unpackedDate;
	}
	
	/**
	 * Takes num value of a month and returns its corresponding localized string
	 * @param month Numerical value of Calendar month 
	 * @return localized String value of requested month
	 */
	private String getLocalizedMonth(int month) {
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] strMonths = dfs.getMonths();
		return strMonths[month];
	}
	
	/**
	 * Create new table with initial values
	 * @param noteText note's main text
	 * @param dateCreated packed int of the date note was created
	 * @param imgFilePath file path to the attached image
	 * @param alarmTime milliseconds of time the alarm was set for; 0 if none set 
	 * @return row id of the newly inserted row, or -1 if error
	 */
	public long createNote(String noteText, int dateCreated, String imgFilePath,
			long alarmTime) {
		ContentValues initVal = new ContentValues();
		initVal.put(PNKEY_NOTE_TEXT, noteText);
		initVal.put(PNKEY_DATE_CREATED, dateCreated);
		if(imgFilePath != null)
			initVal.put(PNKEY_NOTE_IMG, imgFilePath);
		initVal.put(PNKEY_ALARM, alarmTime);
		
		return mDb.insert(PN_DATABASE_TABLE_NAME, null, initVal);
	}

	// Updates the note in the database for all columns except date created
	public boolean updateNote(long rowId, String noteText, String imgFilePath, 
			int dateLastPrayed, long alarmTime) {
		// Not allowing for the date created to be updated
		ContentValues newVal = new ContentValues();
		newVal.put(PNKEY_NOTE_TEXT, noteText);
		newVal.put(PNKEY_NOTE_IMG, imgFilePath);
		newVal.put(PNKEY_LAST_PRAYED, dateLastPrayed);
		newVal.put(PNKEY_ALARM, alarmTime);
		
		return mDb.update(PN_DATABASE_TABLE_NAME, newVal, 
				PNKEY_ROWID + " = " + rowId, null) > 0;
	}
	
	/**
	 * Updates the note in the database for the dateLastPrayed column
	 */
	public boolean updateNote(long rowId, int dateLastPrayed) {
		ContentValues newVal = new ContentValues();
		newVal.put(PNKEY_LAST_PRAYED, dateLastPrayed);
		
		return mDb.update(PN_DATABASE_TABLE_NAME, newVal, 
				PNKEY_ROWID + " = " + rowId, null) > 0;
	}
	
	/**
	 * Deletes the note at the provided row id
	 * @param rowId PNKEY_ROWID to match
	 * @return true if a note was deleted.  false otherwise.
	 */
	public boolean deleteNote(long rowId) {
		return mDb.delete(	PN_DATABASE_TABLE_NAME, 
							PNKEY_ROWID + " = " + rowId,
							null) > 0;
	}
	
	/**
	 * Gets all notes in the database
	 * @return Cursor to all retrieved rows
	 */
	public Cursor getAllNotes() {
		return mDb.query(PN_DATABASE_TABLE_NAME, 
				new String[] {PNKEY_ROWID, PNKEY_NOTE_TEXT, PNKEY_NOTE_IMG, 
				PNKEY_DATE_CREATED, PNKEY_LAST_PRAYED, PNKEY_ALARM}, 
				null, null, null, null, PNKEY_ROWID+" DESC");
		//ORDER BY _id DESC will place newest notes at top of the list
	}
	
	/**
	 * Gets the note at the provided row id
	 * @param rowId PNKEY_ROWID to match
	 * @return Cursor for matching row in the database.  null otherwise.
	 */
	public Cursor getNote(long rowId) throws SQLException {
		Cursor cursor =
			mDb.query(true, PN_DATABASE_TABLE_NAME, 
					new String[] {PNKEY_ROWID, PNKEY_NOTE_TEXT, PNKEY_NOTE_IMG, 
					PNKEY_DATE_CREATED, PNKEY_LAST_PRAYED, PNKEY_ALARM}, 
					PNKEY_ROWID + " = " + rowId,
					null, null, null, null, null);
		
		boolean bSuccess = false;
		if( cursor != null ) {
			bSuccess = cursor.moveToFirst();
		}
		
		return bSuccess ? cursor : null;
	}
	
	////////////////////////////////////////////////////////////
	// PNDatabaseHelper
	//   Custom database helper for creating and upgrading database
	//   TODO: make this a singleton?
	////////////////////////////////////////////////////////////
	private static class PNDatabaseHelper extends SQLiteOpenHelper {
		
		//
		PNDatabaseHelper(Context context) {
			super(context, PN_DATABASE_FILE_NAME, null, PN_DATABASE_VERSION);
		}

		//
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(PN_DATABASE_CREATE);
		}

		//
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(PN_DATABASE_TABLE_NAME, "Upgrading database from version " +
					oldVersion + " to " + newVersion+ ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + PN_DATABASE_TABLE_NAME);
			onCreate(db);
		}
	}
}
