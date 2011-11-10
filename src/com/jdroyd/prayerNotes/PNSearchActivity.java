/**
 * PNSearchActivity
 * 
 * Activity used to display the search results.  Should be similar to the main
 * page but with more limited available actions.
 */

package com.jdroyd.prayerNotes;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class PNSearchActivity extends ListActivity {
	// Logging tag
	private static final String TAG = "PNSearchActivity";
	
	// Database handle
	private PNDbAdapter mDbAdapter;
	
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_results);
		setTitle(R.string.app_name);
		
		Log.v(TAG, "PNSearchActivity.onCreate()");
		
		mDbAdapter = new PNDbAdapter(this);
        mDbAdapter.open();
		
		handleIntent(getIntent());
		
		// Register ListView for context menus
        registerForContextMenu(getListView());
	}
	
	/**
	 * Since this activity uses singleTop launch mode, a new search will trigger
	 * onNewIntent() instead of onCreate()
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		Log.v(TAG, "PNSearchActivity.onNewIntent()");
		
	    setIntent(intent);
	    handleIntent(intent);
	}

	/**
	 * Gets query from the intent and displays search results
	 * @param intent contains query data
	 */
	private void handleIntent(Intent intent) {
	    // Receive the search query
		String query = null;
		if( Intent.ACTION_SEARCH.equals(intent.getAction()) ) {
			query = intent.getStringExtra(SearchManager.QUERY);
			Log.v(TAG, "query: "+query);
		}
		
		// Display any results to the list
		populateList(query);
	}
	
	/**
     * Called when another Activity launched by this application exits
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	
    	// Beacuse we're returning from another Activity, database was likely
    	// closed by onPause(), and so needs to be re-opened
    	if( mDbAdapter != null )
    		mDbAdapter.open();
    	
    	populateList(null);
    }
    
    /**
     * Start Activity to edit note when a ListView item is clicked
     */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		long rowId = getRowIdFromListAtPosition(position);
		editPrayerNote(rowId);
	}
	
	/**
     * 
     */
	@SuppressWarnings("unchecked")
    private long getRowIdFromListAtPosition(int position) {
    	HashMap<String,Object> tmp = (HashMap<String,Object>)getListView().getItemAtPosition(position);
		int rowId = (Integer)tmp.get(PNDbAdapter.PNKEY_ROWID);
		return (long)rowId;
    }
	
	/**
     * Launch new activity to edit note
     * @param position specific note to edit in the list
     */
    private void editPrayerNote(long position) {
    	Intent i = new Intent(this, PNEditNote.class);
    	i.putExtra(PNDbAdapter.PNKEY_ROWID, position);
    	
    	startActivityForResult(i, Constants.ACTIVITY_EDIT);
    }
    
    
    /**
     * Retrieves data from the database and sets it to display in the ListView
     * @param query String to filter notes with
     */
    private void populateList(String query) {
    	Cursor notesCursor = null;
    	if( query == null ) {
    		Log.e(TAG,"query = null");
    		return;
    	}
    	// If query string is provided, then display only matching notes
    	else {
    		Log.v(TAG,"query = ["+query+"]");
    		notesCursor = mDbAdapter.getNotesWithText(query);
    	}
    	
    	startManagingCursor(notesCursor);
    	
    	ArrayList <HashMap<String,Object>> noteList = 
    		new ArrayList<HashMap<String,Object>>();
    	
    	if( notesCursor != null ) {
    		if( notesCursor.moveToFirst() ) {
	    		while( notesCursor.isAfterLast() == false ) {
	    			
	    			int noteId = notesCursor.getInt(
	    					notesCursor.getColumnIndexOrThrow(PNDbAdapter.PNKEY_ROWID) );
	    			String noteText = notesCursor.getString( 
	    					notesCursor.getColumnIndexOrThrow(PNDbAdapter.PNKEY_NOTE_TEXT) );
	    			String noteImg = notesCursor.getString( 
	    					notesCursor.getColumnIndexOrThrow(PNDbAdapter.PNKEY_NOTE_IMG) );
	    			int dateCreated = notesCursor.getInt( 
	    					notesCursor.getColumnIndexOrThrow(PNDbAdapter.PNKEY_DATE_CREATED) );
	    			int lastPrayed = notesCursor.getInt( 
	    					notesCursor.getColumnIndexOrThrow(PNDbAdapter.PNKEY_LAST_PRAYED) );
	    			
	    			HashMap<String,Object> noteHash = new HashMap<String,Object>();
	    			
	    			// Row id also needs to be saved into hash so that when we
	    			// select this item in onListItemClick, we have its id to reference
	    			noteHash.put(PNDbAdapter.PNKEY_ROWID, noteId);
	    			
	    			// Get note text
	    			if( noteText != null ) {
	    				noteHash.put(PNDbAdapter.PNKEY_NOTE_TEXT, noteText);
	    			}
	    			
	    			// Get image data
	    			if( noteImg != null ) {
	    				noteHash.put(PNDbAdapter.PNKEY_NOTE_IMG, noteImg);
	    			}
	    			else {
	    				noteHash.put(PNDbAdapter.PNKEY_NOTE_IMG, null);
	    			}
	    			
	    			// Get date created
	    			String strDateCreated = getResources().getText(R.string.date_unknown).toString();
	    			if( dateCreated > 0 ) {
	    				strDateCreated = mDbAdapter.convertDbDateToString(dateCreated);
	    			}
	    			noteHash.put(PNDbAdapter.PNKEY_DATE_CREATED, strDateCreated);
	    			
	    			// Get LastPrayed data
	    			String strLastPrayed = getResources().getText(R.string.date_never).toString();
	    			if( lastPrayed > 0 ) {
	    				strLastPrayed = mDbAdapter.convertDbDateToString(lastPrayed);
	    			}
	    			noteHash.put(PNDbAdapter.PNKEY_LAST_PRAYED, strLastPrayed);
	    			
	    			// Add recovered data into the ArrayList
	    			noteList.add(noteHash);
	    			
	    			notesCursor.moveToNext();
	    		}
    		}
    	}
    	
    	// Sort the list.  Notes not prayed for are moved to the top.
    	int listEnd = noteList.size();
    	for( int i=0; i<listEnd; i++) {
    		HashMap<String, Object> hash = noteList.get(i);
    		String lastPrayed = (String)hash.get(PNDbAdapter.PNKEY_LAST_PRAYED);
    		
    		// If it's been prayed for, move to the end of the list
    		// checking against "Never" string
    		if( lastPrayed != getResources().getText(R.string.date_never).toString() ) {
    			// add copy of note to end of list
    			noteList.add(hash);
    			// remove it from its old position
    			noteList.remove(i);
    			// decrement so that we don't re-evaluate moved notes
    			listEnd--;
    		}
    	}
    	
    	String[] fromColumns = new String[]{
    			PNDbAdapter.PNKEY_NOTE_TEXT, PNDbAdapter.PNKEY_NOTE_IMG,
    			PNDbAdapter.PNKEY_DATE_CREATED, PNDbAdapter.PNKEY_LAST_PRAYED};
    	
    	int[] toFields = new int[]{
    			R.id.main_note_row_text, R.id.main_note_row_img,
    			R.id.main_note_row_created, R.id.main_note_row_status};

    	PNViewAdapter notes = new PNViewAdapter(this, noteList,
    			R.layout.main_note_row, fromColumns, toFields);
    	
    	setListAdapter(notes);
    	
    	ListView lv = getListView();
    	lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }
}
