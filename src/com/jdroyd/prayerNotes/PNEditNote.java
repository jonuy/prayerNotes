package com.jdroyd.prayerNotes;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class PNEditNote extends Activity implements OnClickListener {

	//TODO: move to file with all the constants?
	public static final int ACTIVITY_IMG_GALLERY = 3;
	
	private PNDbAdapter mDbAdapter;
	private EditText mNoteText;
	private Long mDbRowId;
	
	private Button mSaveButton;
	private Button mShareButton;
	private Button mDiscardButton;
	private ImageView mImgView;
	
	// Selected file path of image, if any
	private String mImgFilePath;
	// Stored date note was created, if applicable
	private int mDateCreated;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_note);
		setTitle(R.string.edit_note_title);
		
		mDbAdapter = new PNDbAdapter(this);
		mDbAdapter.open();
		
		mNoteText = (EditText)findViewById(R.id.edit_note_text);
		mSaveButton = (Button)findViewById(R.id.edit_note_save);
		mShareButton = (Button)findViewById(R.id.edit_note_share);
		mDiscardButton = (Button)findViewById(R.id.edit_note_discard);
		mImgView = (ImageView)findViewById(R.id.edit_note_img);

		mSaveButton.setOnClickListener(this);
		mShareButton.setOnClickListener(this);
		mDiscardButton.setOnClickListener(this);
		mImgView.setOnClickListener(this);
		
		// Get row id of note being edited, if any
		mDbRowId = (savedInstanceState == null) ? null :
			(Long)savedInstanceState.getSerializable(PNDbAdapter.PNKEY_ROWID);
		if( mDbRowId == null ) {
			Bundle extras = getIntent().getExtras();
			mDbRowId = (extras==null) ? null : extras.getLong(PNDbAdapter.PNKEY_ROWID);
		}
		Log.v("PN", "PNEditeNote activity started with mDbRowId = "+mDbRowId);
		
		populateFields();
	}
	
	/**
	 * If a row id has been provided, gets the corresponding data from the
	 * database and populates the corresponding fields in the View.
	 */
	private void populateFields() {
		if( mDbRowId != null && mDbAdapter != null) {
			Cursor note = mDbAdapter.getNote(mDbRowId);
			if( note != null ) {
				// Android method that takes care of Cursor life-cycle and resources
				startManagingCursor(note);
				
				if( mNoteText != null ) {
					mNoteText.setText( note.getString(
							note.getColumnIndexOrThrow(PNDbAdapter.PNKEY_NOTE_TEXT)) );
				}
				
				mDateCreated = note.getInt(
						note.getColumnIndexOrThrow(PNDbAdapter.PNKEY_DATE_CREATED));
				
				mImgFilePath = note.getString(
						note.getColumnIndexOrThrow(PNDbAdapter.PNKEY_NOTE_IMG));
				if( mImgFilePath != null ) {
					setNoteImageToView(mImgFilePath);
				}
				// TODO: display field for date created?
				// TODO: display field for last prayed for?
			}
			else {
				Log.w("PN", "No note found at row id: "+mDbRowId);
			}
		}
	}

	/**
	 * onClick listener.  Handles behavior when UI elements are clicked
	 */
	@Override
	public void onClick(View v) {
		switch( v.getId() ) {
		case R.id.edit_note_save:
			saveState();
			setResult(RESULT_OK);
			finish();
			break;
		case R.id.edit_note_share:
			setResult(RESULT_OK);
			Toast.makeText(v.getContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
			break;
		case R.id.edit_note_discard:
			setResult(RESULT_OK);
			finish();
			break;
		case R.id.edit_note_img:
			startGalleryActivity();
			break;
		}
	}
	
	/**
	 * Save data to the database
	 */
	private void saveState() {
		if( mDbAdapter != null )
			mDbAdapter.open();
		
		String noteText = mNoteText.getText().toString();
		
		if( mDbRowId == null ) {
			long id = mDbAdapter.createNote(noteText, mDbAdapter.getCurrentDateForDb(), 
					mImgFilePath);
			if( id >= 0 )
				mDbRowId = id;
		}
		else {
			mDbAdapter.updateNote(mDbRowId, noteText, mDateCreated, mImgFilePath);
		}
	}
	
	/**
	 * Starts image gallery Activity to pick an image
	 */
	private void startGalleryActivity() {
		Intent i = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //or INTERNAL_CONTENT_URI?
		startActivityForResult(i, ACTIVITY_IMG_GALLERY);
	}
	
	/**
	 * Handles the results of Activities launched from this class
	 */
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	
    	switch( requestCode ) {
    	case ACTIVITY_IMG_GALLERY:
    		if(resultCode == RESULT_OK) {
    			Log.v("PN", "IMG GALLERY Activity returned OK");
    			
				Uri selectedImage = intent.getData();
				String[] filePathColumn = {MediaStore.Images.Media.DATA};
				
				Cursor cursor = getContentResolver().query(selectedImage, 
					filePathColumn, null, null, null);
				startManagingCursor(cursor);
				cursor.moveToFirst();
				
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				mImgFilePath = cursor.getString(columnIndex);
				//cursor.close(); - hopefully shouldn't need to do this because of use of startManagingCursor?
				
				Log.v("PN", "img file path: "+mImgFilePath);
				
				setNoteImageToView(mImgFilePath);
    		}
    		else {
    			Log.v("PN", "IMG GALLERY Activity returned: "+resultCode);
    		}
    		break;
    	}
    }
	
	private void setNoteImageToView(String strFilePath) {
		Bitmap selectedImg = BitmapFactory.decodeFile(strFilePath);
		if( mImgView != null ) {
			//TODO: adjust frame size smarter (*1.5 will just make it continue to get larger)
			LayoutParams frame = mImgView.getLayoutParams();
			frame.width = (int)(frame.width * 1.5);
			frame.height = (int)(frame.height * 1.5);
			mImgView.setLayoutParams(frame);
			
			mImgView.setImageBitmap(selectedImg);
		}
	}
	
	// Always called when the Activity ends
    // TODO: save the current note back to the database?
	@Override
	protected void onPause() {
		super.onPause();
		// do we wanna do this?  does state get saved when moving to image
		// gallery or other activity?  -- seems so...
		//saveState();
		
		mDbAdapter.close();
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
