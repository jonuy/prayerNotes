package com.jdroyd.prayerNotes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class PrayerNotes extends ListActivity {
	
	//TODO: make this an enum?  place in constants file?
	public static final int ACTIVITY_CREATE = 0;
	public static final int ACTIVITY_EDIT = 1;
	
	public static final int CONTEXT_DELETE_ID = 0;
	public static final int CONTEXT_PRAYED_ID = 1;
	
	private static final int DIALOG_DELETE_NOTE = 0;
	
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
        //setTitle(R.string.app_name)
        
        ////// DEV DELETE DATABASE //////
        //boolean deleteResult = this.deleteDatabase(PNDbAdapter.PN_DATABASE_FILE_NAME);
        //Log.v("PN", "Database delete result: "+deleteResult);
        /////////////////////////////////
        
        mDbAdapter = new PNDbAdapter(this);
        mDbAdapter.open();
        
        // Display any existing data to the ListView
        populateList();
        
        // Setup click listener for the Add button
        Button addButton = (Button)findViewById(R.id.main_button_add);
        addButton.setOnClickListener(new View.OnClickListener() {        	
        	public void onClick(View view) {
        		createPrayerNote();
        	}
        });
        
        // Register ListView for context menus
        registerForContextMenu(getListView());
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
    	startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    /**
     * Launch new activity to edit note
     * @param position specific note to edit in the list
     */
    private void editPrayerNote(long position) {
    	Intent i = new Intent(this, PNEditNote.class);
    	i.putExtra(PNDbAdapter.PNKEY_ROWID, position);
    	
    	startActivityForResult(i, ACTIVITY_EDIT);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	
    	populateList();
    }
    
    /**
     * Retrieves data from the database and sets it to display in the ListView
     */
    private void populateList() {
    	// Get all of the rows from the database
    	// TODO: should only get the 10 or 20 most recent
    	// TODO: notes that aren't marked as prayed-for should go first
    	Cursor notesCursor = mDbAdapter.getAllNotes();
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
	    			
	    			int pos = notesCursor.getPosition();
	    			Log.v("populateList()", pos+": noteId="+noteId+" / noteText:"
	    					+noteText+" / noteImg:"+noteImg+" / dateCreated:"
	    					+dateCreated+" / lastPrayed:"+lastPrayed);
	    			
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
    // PNViewAdapter
    //   Custom adapter used to display the info in the main ListView
    ////////////////////////////////////////////////////////////////////////////
    private class PNViewAdapter extends SimpleAdapter {

		public PNViewAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
		}
		
		@Override
		public void setViewImage(ImageView v, int value) {
			super.setViewImage(v, value);
			
			if(v.getId() == R.id.main_note_row_img) {
				if( value > 0 )
					v.setVisibility(View.VISIBLE);
				else
					v.setVisibility(View.GONE);
			}
		}
		
		@Override
		public void setViewImage(ImageView v, String value) {
			// Consume setViewImage if value is an empty string, otherwise
			// super tries to evaluate it as a file path and throws errors
			if( value != "" ) {
				super.setViewImage(v, value);
				//Is this way faster than super.setViewImage()?
				//Bitmap img = BitmapFactory.decodeFile(value);
				//v.setImageBitmap(img);
				
				if(v.getId() == R.id.main_note_row_img) {
					v.setVisibility(View.VISIBLE);
				}
			}
			else {
				if(v.getId() == R.id.main_note_row_img) {
					v.setVisibility(View.GONE);
				}
			}
		}

		@Override
		public void setViewText(TextView v, String text) {
			super.setViewText(v, text);
		}    	
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Context menu function overrides
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
    		ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	menu.add(0, CONTEXT_PRAYED_ID, 0, R.string.context_menu_prayed);
    	menu.add(0, CONTEXT_DELETE_ID, 0, R.string.context_menu_delete);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	Long rowId = null;
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	if( info != null )
    		rowId = getRowIdFromListAtPosition(info.position);
    	
    	if( rowId != null ) {
	    	switch(item.getItemId()) {
	    	case CONTEXT_DELETE_ID:
	    		// Set row id for dialog to delete
				mRowIdToDelete = rowId;
	    		showDialog(DIALOG_DELETE_NOTE);
	    		return true;
	    	case CONTEXT_PRAYED_ID:
	    		if( mDbAdapter != null ) {
		    		// Get current time and save to database
		    		mDbAdapter.updateNote(rowId, mDbAdapter.getCurrentDateForDb());
		    		// Display Toast acknowledgment
		    		Toast.makeText(this, R.string.context_prayed_success, Toast.LENGTH_SHORT)
		    			 .show();
		    		// refresh ListView
		    		populateList();
	    		}
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
		case DIALOG_DELETE_NOTE:
			return createDeleteDialog();
		}

		return super.onCreateDialog(id);
	}
}