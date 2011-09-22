/**
 * TODO: move some of the public static final vars into their own
 *   constants class?
 */

package com.jdroyd.prayerNotes;

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
	
	/**
	 * Database creation sql statement
	 */
	private static final String PN_DATABASE_CREATE =
		"CREATE TABLE " + PN_DATABASE_TABLE_NAME + "("
		+ PNKEY_ROWID + " " + PNKEY_ROWID_TYPE + " " + PNKEY_ROWID_ARGS + ", "
		+ PNKEY_NOTE_TEXT + " " + PNKEY_NOTE_TEXT_TYPE + " " + PNKEY_NOTE_TEXT_ARGS + ", "
		+ PNKEY_NOTE_IMG + " " + PNKEY_NOTE_IMG_TYPE + " " + PNKEY_NOTE_IMG_ARGS + ", "
		+ PNKEY_DATE_CREATED + " " + PNKEY_DATE_CREATED_TYPE + " " + PNKEY_DATE_CREATED_ARGS + ", "
		+ PNKEY_LAST_PRAYED + " " + PNKEY_LAST_PRAYED_TYPE + " " + PNKEY_LAST_PRAYED_ARGS
		+ ");";
	
	// Name of the database file
	private static final String PN_DATABASE_FILE_NAME = "prayerNotesDb";
	
	// Database version number
	// onUpgrade() used if Db version is upgraded
	private static final int PN_DATABASE_VERSION = 2;
	
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
		mDbHelper = new PNDatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
	/**
	 * 
	 */
	public void close() {
		mDbHelper.close();
	}
	
	//TODO:  createNote() with all possible columns for params?
	// isn't there a design pattern to handle this?  check Effective Java book.
	public long createNote(String noteText, int dateCreated) {
		ContentValues initVal = new ContentValues();
		initVal.put(PNKEY_NOTE_TEXT, noteText);
		initVal.put(PNKEY_DATE_CREATED, dateCreated);
		
		return mDb.insert(PN_DATABASE_TABLE_NAME, null, initVal);
	}

	//TODO: updateNote() with all the other possible columns?
	public boolean updateNote(long rowId, String noteText, int dateCreated) {
		ContentValues newVal = new ContentValues();
		newVal.put(PNKEY_NOTE_TEXT, noteText);
		newVal.put(PNKEY_DATE_CREATED, dateCreated);
		
		return mDb.update(PN_DATABASE_TABLE_NAME, newVal, 
				PNKEY_ROWID + " = " + rowId, null) > 0;
	}
	
	/**
	 * 
	 * @param rowId
	 * @return
	 */
	public boolean deleteNote(long rowId) {
		return mDb.delete(	PN_DATABASE_TABLE_NAME, 
							PNKEY_ROWID + " = " + rowId,
							null) > 0;
	}
	
	/**
	 * 
	 * @return
	 */
	public Cursor getAllNotes() {
		return mDb.query(PN_DATABASE_TABLE_NAME, 
				new String[] {PNKEY_ROWID, PNKEY_NOTE_TEXT, PNKEY_NOTE_IMG, 
				PNKEY_DATE_CREATED, PNKEY_LAST_PRAYED}, 
				null, null, null, null, null);
	}
	
	/**
	 * 
	 * @param rowId
	 * @return
	 */
	public Cursor getNote(long rowId) throws SQLException {
		Cursor cursor =
			mDb.query(true, PN_DATABASE_TABLE_NAME, 
					new String[] {PNKEY_ROWID, PNKEY_NOTE_TEXT, PNKEY_NOTE_IMG, 
					PNKEY_DATE_CREATED, PNKEY_LAST_PRAYED}, 
					PNKEY_ROWID + " = " + rowId,
					null, null, null, null, null);
		
		if( cursor != null ) {
			cursor.moveToFirst();
		}
		
		return cursor;
	}
	
	////////////////////////////////////////////////////////////
	// PNDatabaseHelper
	//   Custom database helper for creating and upgrading database
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
