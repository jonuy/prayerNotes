package com.jdroyd.prayerNotes;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class PrayerNotes extends ListActivity {
	
	//
	private PNDbAdapter mDbAdapter;
	
	// Row id of note to delete
	private Long mRowIdToDelete;
	
    /** 
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_list);
        setTitle(R.string.app_name);
        
        ////// DEV DELETE DATABASE //////
        //boolean deleteResult = this.deleteDatabase(PNDbAdapter.PN_DATABASE_FILE_NAME);
        //Log.v("PN", "Database delete result: "+deleteResult);
        /////////////////////////////////
        
        mDbAdapter = new PNDbAdapter(this);
        mDbAdapter.open();
        
        // Display any existing data to the ListView
		populateList();
        
        // Setup click listener for the buttons
        ImageButton addButton = (ImageButton)findViewById(R.id.main_button_add);
        addButton.setOnClickListener(new View.OnClickListener() {        	
        	@Override
        	public void onClick(View view) {
        		createPrayerNote();
        	}
        });
        ImageButton searchButton = (ImageButton)findViewById(R.id.main_button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
        	@Override
			public void onClick(View v) {
        		onSearchRequested();
			}
        });
        
        // Register ListView for context menus
        registerForContextMenu(getListView());
    }
    
    /**
     * Display a Toast message for unfinished features
     */
    private void displayComingSoon() {
    	Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
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
     * Launch a new activity to create a note
     */
    private void createPrayerNote() {
    	Intent i = new Intent(this, PNEditNote.class);
    	startActivityForResult(i, Constants.ACTIVITY_CREATE);
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
     * Called when another Activity launched by this application exits
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	
    	// Beacuse we're returning from another Activity, database was likely
    	// closed by onPause(), and so needs to be re-opened
    	if( mDbAdapter != null )
    		mDbAdapter.open();
    	
    	populateList();
    }
    
    /**
     * Retrieves data from the database and sets it to display in the ListView
     */
    private void populateList() {
    	// TODO: should only get the 10 or 20 most recent
    	
    	Cursor notesCursor = null;
    	notesCursor = mDbAdapter.getAllNotes();
    	
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
    	
    	/*for( Iterator<HashMap<String, Object>> it = noteList.iterator(); it.hasNext(); ) {
    		HashMap<String, Object> hash = (HashMap<String, Object>)it.next();
	    	int noteId = (Integer)hash.get(PNDbAdapter.PNKEY_ROWID);
			String noteText = (String)hash.get(PNDbAdapter.PNKEY_NOTE_TEXT);
			String noteImg = (String)hash.get(PNDbAdapter.PNKEY_NOTE_IMG);
			String dateCreated = (String)hash.get(PNDbAdapter.PNKEY_DATE_CREATED);
			String lastPrayed = (String)hash.get(PNDbAdapter.PNKEY_LAST_PRAYED);
	    	Log.v("SORT","noteId="+noteId+" / noteText:"
					+noteText+" / noteImg:"+noteImg+" / dateCreated:"
					+dateCreated+" / lastPrayed:"+lastPrayed);
    	}*/
    	
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
    
    ////////////////////////////////////////////////////////////////////////////
    // Options/Action Menu  ...  Action Bar only available at SDK 3.0+
    ////////////////////////////////////////////////////////////////////////////
    /**
     * System calls this to create the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    /**
     * Handle option menu item selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.main_button_add:
            displayComingSoon();
            return true;
        case R.id.main_button_search:
            displayComingSoon();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Context menu function overrides
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
    		ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	menu.add(0, Constants.CONTEXT_OPEN_ID, 0, R.string.context_menu_open);
    	menu.add(0, Constants.CONTEXT_PRAYED_ID, 0, R.string.context_menu_prayed);
    	menu.add(0, Constants.CONTEXT_DELETE_ID, 0, R.string.context_menu_delete);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	Long rowId = null;
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	if( info != null )
    		rowId = getRowIdFromListAtPosition(info.position);
    	
    	if( rowId != null ) {
	    	switch(item.getItemId()) {
	    	case Constants.CONTEXT_DELETE_ID:
	    		// Set row id for dialog to delete
				mRowIdToDelete = rowId;
	    		showDialog(Constants.DIALOG_DELETE_NOTE);
	    		return true;
	    	case Constants.CONTEXT_PRAYED_ID:
	    		if( mDbAdapter != null ) {
		    		// Get current time and save to database
		    		mDbAdapter.updateNote(rowId, mDbAdapter.getCurrentDateForDb());
		    		// Display Toast acknowledgment
		    		Toast.makeText(this, R.string.context_prayed_success, Toast.LENGTH_LONG)
		    			 .show();
		    		// refresh ListView
		    		populateList();
	    		}
	    		return true;
	    	case Constants.CONTEXT_OPEN_ID:
	    		editPrayerNote(rowId);
	    		return true;
	    	}
    	}
    	
    	return super.onContextItemSelected(item);
    }
    
    /**
     * Creates dialog box prompting user to confirm delete action
     */
    private AlertDialog createDeleteDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.dialog_delete_confirm)
			.setCancelable(false)
			.setPositiveButton(R.string.dialog_yes,	new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Delete note from database
					if( mDbAdapter != null && mRowIdToDelete != null ) {
						if( mDbAdapter.deleteNote(mRowIdToDelete) ) {
							// Reset value if row was successfully deleted
							mRowIdToDelete = null;
							// Repopulate list since row's been deleted
							populateList();
						}
					}
				}
			})
			.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Dismiss dialog with no changes made
					dialog.cancel();
				}
			});
		
		AlertDialog alert = builder.create();
		return alert;
	}

    /**
	 * Called first time showDialog() is called for a given id
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case Constants.DIALOG_DELETE_NOTE:
			return createDeleteDialog();
		}

		return super.onCreateDialog(id);
	}
	
	/**
	 *  Called when the Activity ends
	 */
	@Override
	protected void onPause() {
		super.onPause();
		
		if( mDbAdapter != null )
			mDbAdapter.close();
	}
}