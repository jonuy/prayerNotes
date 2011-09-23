package com.jdroyd.prayerNotes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class PrayerNotes extends ListActivity {
	
	//TODO: make this an enum?  place in constants file?
	public static final int ACTIVITY_CREATE = 0;
	public static final int ACTIVITY_EDIT = 1;
	
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
        
        // Setup click listener for the Add button
        Button addButton = (Button)findViewById(R.id.main_button_add);
        addButton.setOnClickListener(new View.OnClickListener() {        	
        	public void onClick(View view) {
        		createPrayerNote();
        	}
        });
    }
    
    private void createPrayerNote() {
    	Intent i = new Intent(this, PNEditNote.class);
    	startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    /**
     * Launch new activity to edit note
     * @param position 
     */
    private void editPrayerNote(int position) {
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
     * 
     */
    private void populateList() {
    	/*
    	// Get all of the rows from the database
    	// TODO: should only get the 10 or 20 most recent
    	// TODO: notes that aren't marked as prayed-for should go first
    	Cursor notesCursor = mDbAdapter.getAllNotes();
    	startManagingCursor(notesCursor);
    	
    	// TODO: build ArrayList from data in Cursor
    	*/
    	
    	ArrayList <HashMap<String,Object>> tmpList = 
    		new ArrayList<HashMap<String,Object>>();
    	
    	HashMap<String,Object> tmp;
    	
    	tmp = new HashMap<String,Object>();
    	tmp.put(PNDbAdapter.PNKEY_NOTE_TEXT, "This is a note #1 with a lot more text that will hopefully wrap and wrap and wrap and wrap.");
    	//tmp.put(PNDbAdapter.PNKEY_NOTE_IMG, R.drawable.icon);
    	tmp.put(PNDbAdapter.PNKEY_DATE_CREATED, "Sat, Sep 24, 2011");
    	tmp.put(PNDbAdapter.PNKEY_LAST_PRAYED, "Never");
    	tmpList.add(tmp);
    	
    	tmp = new HashMap<String,Object>();
    	tmp.put(PNDbAdapter.PNKEY_NOTE_TEXT, "This is a note #2.");
    	tmp.put(PNDbAdapter.PNKEY_NOTE_IMG, R.drawable.icon);
    	tmp.put(PNDbAdapter.PNKEY_DATE_CREATED, "Fri, Sep 23, 2011");
    	tmp.put(PNDbAdapter.PNKEY_LAST_PRAYED, "Yesterday");
    	tmpList.add(tmp);
    	
    	tmp = new HashMap<String,Object>();
    	tmp.put(PNDbAdapter.PNKEY_NOTE_TEXT, "And then this is the note #3 with a lot more text that will hopefully wrap and wrap and wrap and wrap.");
    	tmp.put(PNDbAdapter.PNKEY_DATE_CREATED, "Sat, Sep 24, 2124");
    	tmp.put(PNDbAdapter.PNKEY_LAST_PRAYED, "Never");
    	tmpList.add(tmp);
    	
    	String[] fromColumns = new String[]{PNDbAdapter.PNKEY_NOTE_TEXT,
    			PNDbAdapter.PNKEY_NOTE_IMG, PNDbAdapter.PNKEY_DATE_CREATED,
    			PNDbAdapter.PNKEY_LAST_PRAYED};
    	
    	int[] toFields = new int[]{R.id.main_note_row_text,
    			R.id.main_note_row_img, R.id.main_note_row_created,
    			R.id.main_note_row_status};

    	PNViewAdapter notes = new PNViewAdapter(this, tmpList,
    			R.layout.main_note_row, fromColumns, toFields);
    	
    	setListAdapter(notes);
    	
    	ListView lv = getListView();
    	lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }
    
    /**
     * 
     */
    private class PNViewAdapter extends SimpleAdapter {

		public PNViewAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
		}
		
		@Override
		public void setViewImage(ImageView v, int value) {
			super.setViewImage(v, value);
			
			if(v.getId() == R.id.main_note_row_img && value > 0) {
				v.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void setViewText(TextView v, String text) {
			super.setViewText(v, text);
			
			if(v.getId() == R.id.main_note_row_img && text != null) {
				v.setVisibility(View.VISIBLE);
			}
		}    	
    }
}