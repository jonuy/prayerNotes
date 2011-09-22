package com.jdroyd.prayerNotes;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

public class PrayerNotes extends ListActivity {
	
	//
	private PNDbAdapter mDbAdapter;
	
	//TODO: TEMPORARY.  DELETE ME.
	private int tmpNoteCounter=0;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_list);
        
        mDbAdapter = new PNDbAdapter(this);
        mDbAdapter.open();
        
        populateList();
        
        // TODO: temp until we can get the ADD button hooked up to new Activity
        // TEMPORARY.  DELETE ME.
        Button addButton = (Button)findViewById(R.id.main_button_add);
        addButton.setOnClickListener(new View.OnClickListener() {
        	
        	public void onClick(View view) {
        		String noteText = "pn text " + tmpNoteCounter;
        		int dateCreated = 169674688;
        		mDbAdapter.createNote(noteText, dateCreated);
        		tmpNoteCounter++;
        		
        		populateList();
        	}
        });
        // end TEMPORARY.  DELETE ME.
    }
    
    /**
     * 
     */
    private void populateList() {
    	// Get all of the rows from the database
    	// TODO: should only get the 10 or 20 most recent
    	// TODO: notes that aren't marked as prayed-for should go first
    	Cursor notesCursor = mDbAdapter.getAllNotes();
    	startManagingCursor(notesCursor);
    	
    	// Column names to bind to the UI
    	// TODO: how can we make this work for the images too?
    	// TODO: and then all the status update texts too...
    	String[] fromColumns = new String[]{PNDbAdapter.PNKEY_NOTE_TEXT};

    	// Views that will display the text in fromColumns
    	int[] toFields = new int[]{R.id.main_note_row_text};
    	
    	// map columns from a Cursor to the TextView
    	SimpleCursorAdapter notes =
    		new SimpleCursorAdapter(this, R.layout.main_note_row,
    				notesCursor, fromColumns, toFields);
    	setListAdapter(notes);
    }
}