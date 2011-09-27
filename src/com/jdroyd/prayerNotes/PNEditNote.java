package com.jdroyd.prayerNotes;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PNEditNote extends Activity implements OnClickListener {

	private PNDbAdapter mDbHelper;
	private EditText mNoteText;
	private Long mDbRowId;
	
	private Button mSaveButton;
	private Button mShareButton;
	private Button mDiscardButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_note);
		setTitle(R.string.edit_note_title);
		
		mDbHelper = new PNDbAdapter(this);
		mDbHelper.open();
		
		mNoteText = (EditText)findViewById(R.id.edit_note_text);
		mSaveButton = (Button)findViewById(R.id.edit_note_save);
		mShareButton = (Button)findViewById(R.id.edit_note_share);
		mDiscardButton = (Button)findViewById(R.id.edit_note_discard);

		mSaveButton.setOnClickListener(this);
		mShareButton.setOnClickListener(this);
		mDiscardButton.setOnClickListener(this);
		
		// Get row id of note being edited, if any
		mDbRowId = (savedInstanceState == null) ? null :
			(Long)savedInstanceState.getSerializable(PNDbAdapter.PNKEY_ROWID);
		if( mDbRowId == null ) {
			Bundle extras = getIntent().getExtras();
			mDbRowId = (extras==null) ? null : extras.getLong(PNDbAdapter.PNKEY_ROWID);
		}
		
		//TODO: implement for already existing notes
		//populateFields();
	}

	@Override
	public void onClick(View arg0) {
		switch( arg0.getId() ) {
		case R.id.edit_note_save:
			saveState();
			setResult(RESULT_OK);
			finish();
			break;
		case R.id.edit_note_share:
			setResult(RESULT_OK);
			finish();
			break;
		case R.id.edit_note_discard:
			setResult(RESULT_OK);
			finish();
			break;
		}
	}
	
	/**
	 * Save data to the database
	 */
	private void saveState() {
		String noteText = mNoteText.getText().toString();
		
		if( mDbRowId == null ) {
			long id = mDbHelper.createNote(noteText, mDbHelper.getCurrentDateForDb());
			if( id >= 0 )
				mDbRowId = id;
		}
		else {
			mDbHelper.updateNote(mDbRowId, noteText, mDbHelper.getCurrentDateForDb());
		}
	}
	
	// Always called when the Activity ends
    // We'll use this to save current note back to the DB
	@Override
	protected void onPause() {
		super.onPause();
		// do we wanna do this?
		//saveState();
	}

	// Read note out of DB again and populate the fields
	@Override
	protected void onResume() {
		super.onResume();
		//populateFields();
	}

	// called when Activity is being stopped
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// outState here is the the same savedInstanceState we get in onCreate()
		super.onSaveInstanceState(outState);
		//saveState();
		//outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
	}
}
