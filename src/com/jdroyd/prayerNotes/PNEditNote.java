package com.jdroyd.prayerNotes;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class PNEditNote extends Activity implements OnClickListener {

	//TODO: move to file with all the constants?
	public static final int ACTIVITY_IMG_GALLERY = 3;
	public static final int ACTIVITY_EMAIL_SHARE = 4;
	public static final int ACTIVITY_GMAIL_SHARE = 5;
	private static final int DIALOG_DELETE_NOTE = 0;
	private static final int DIALOG_SHARE_NOTE = 1;
	
	private PNDbAdapter mDbAdapter;
	private EditText mNoteText;
	private Long mDbRowId;
	
	// Handles to UI elements
	private Button mSaveButton;
	private Button mShareButton;
	private Button mDiscardButton;
	private ImageView mImgView;
	private ImageView mRemoveImgIcon;
	private CheckBox mPrayedForCheckBox;
	private TextView mPrayedForStatus;
	
	// Selected file path of image, if any
	private String mImgFilePath;
	// Stored date note was created
	private int mDateCreated;
	// Stored date note was last prayed
	private int mDatePrayed;
	
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
		mRemoveImgIcon = (ImageView)findViewById(R.id.edit_note_img_remove);
		mPrayedForCheckBox = (CheckBox)findViewById(R.id.edit_note_prayedFor);
		mPrayedForStatus = (TextView)findViewById(R.id.edit_note_prayedFor_status);

		mSaveButton.setOnClickListener(this);
		mShareButton.setOnClickListener(this);
		mDiscardButton.setOnClickListener(this);
		mImgView.setOnClickListener(this);
		mRemoveImgIcon.setOnClickListener(this);
		mPrayedForCheckBox.setOnClickListener(this);
		
		// Get row id of note being edited, if any
		mDbRowId = (savedInstanceState == null) ? null :
			(Long)savedInstanceState.getSerializable(PNDbAdapter.PNKEY_ROWID);
		if( mDbRowId == null ) {
			Bundle extras = getIntent().getExtras();
			mDbRowId = (extras==null) ? null : extras.getLong(PNDbAdapter.PNKEY_ROWID);
			Log.v("SAVE", "row id from Intent:"+mDbRowId);
		}
		else
			Log.v("SAVE", "row id from Bundle:"+mDbRowId);
		
		// Setup initial UI for the Activity, handling special cases
		initUI();
	}
	
	/**
	 * Configure initial UI depending on if we're creating a new note or not
	 */
	private void initUI() {
		if( isNewNote() ) {
			// Discard button text is "Discard"
			if( mDiscardButton != null ) {
				mDiscardButton.setText(R.string.edit_discard_button);
			}
		}
		else {
			// Make checkbox elements visible if not a new note
			if( mPrayedForCheckBox != null ) {
				mPrayedForCheckBox.setVisibility(View.VISIBLE);
			}
			if( mPrayedForStatus != null ) {
				mPrayedForStatus.setVisibility(View.VISIBLE);
			}
			
			// Discard button text is "Delete"
			if( mDiscardButton != null ) {
				mDiscardButton.setText(R.string.edit_delete_button);
			}
		}
		
		// If applicable, will fill out UI with the saved note data
		populateFields();
	}
	
	/**
	 * Helper to determine whether or not user is editing an already existing 
	 * note or creating a new one.
	 * @return true if note is new, false if editing already existing one
	 */
	private boolean isNewNote() {
		return (mDbRowId == null) ? true : false;
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
				
				// Note's text
				if( mNoteText != null ) {
					mNoteText.setText( note.getString(
							note.getColumnIndexOrThrow(PNDbAdapter.PNKEY_NOTE_TEXT)) );
				}
				
				// Attached image
				mImgFilePath = note.getString(
						note.getColumnIndexOrThrow(PNDbAdapter.PNKEY_NOTE_IMG));
				if( mImgFilePath != null ) {
					setNoteImageToView(mImgFilePath);
				}
				
				// Date last prayed
				mDatePrayed = note.getInt(
						note.getColumnIndexOrThrow(PNDbAdapter.PNKEY_LAST_PRAYED));
				if( mDatePrayed > 0 ) {
					if( mPrayedForCheckBox != null ) {
						mPrayedForCheckBox.setChecked(true);
						mPrayedForCheckBox.setText(R.string.checkbox_prayed_success_text);
					}
					if( mPrayedForStatus != null && mDbAdapter != null ) {
						String prayedStatus = 
							getResources().getText(R.string.last_prayed_for).toString()
							+ " " + mDbAdapter.convertDbDateToString(mDatePrayed);
						mPrayedForStatus.setText(prayedStatus);
					}
				}
				else {
					if( mPrayedForCheckBox != null ) {
						mPrayedForCheckBox.setChecked(false);
						mPrayedForCheckBox.setText(R.string.checkbox_prayed_for_text);
					}
					if( mPrayedForStatus != null ) {
						String prayedStatus = 
							getResources().getText(R.string.last_prayed_for).toString()
							+" "+ getResources().getText(R.string.date_never).toString();
						mPrayedForStatus.setText(prayedStatus);
					}
				}
				
				// Date created
				mDateCreated = note.getInt(
						note.getColumnIndexOrThrow(PNDbAdapter.PNKEY_DATE_CREATED));
				
				// TODO: display field for date created?
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
			showDialog(DIALOG_SHARE_NOTE);
			break;
		case R.id.edit_note_discard:
			if( isNewNote() ) {
				setResult(RESULT_OK);
				finish();
			}
			else {
				showDialog(DIALOG_DELETE_NOTE);
			}
			break;
		case R.id.edit_note_img:
			startGalleryActivity();
			break;
		case R.id.edit_note_img_remove:
			removeNoteImage();
			break;
		case R.id.edit_note_prayedFor:
			onNoteIsPrayedFor( ((CheckBox)v).isChecked() );
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
			mDbAdapter.updateNote(mDbRowId, noteText, mImgFilePath, mDatePrayed);
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
	 * Method to ensure Activities exist before they get added to the share list
	 */
	private CharSequence[] getAvailableShareActivities() {
		ArrayList<String> strItems = new ArrayList<String>();
		
		// Email Intent
		if( PNShareIntentsHandler.getInstance()
						   .isActivityAvailable(this, PNShareIntentsHandler.PNIntent.EMAIL) ) {
			strItems.add(getResources().getText(R.string.dialog_list_email).toString());
		}
		
		// Gmail Intent
		if( PNShareIntentsHandler.getInstance()
						   .isActivityAvailable(this, PNShareIntentsHandler.PNIntent.GMAIL) ) {
			strItems.add(getResources().getText(R.string.dialog_list_gmail).toString());
		}
		
		int numItems = strItems.size();
		CharSequence[] items = new String[numItems];
		for( int i=0; i<numItems; i++ ) {
			items[i] = strItems.get(i);
		}
		
		return items;
	}
	
	/**
	 * Starts email share Activity
	 */
	private void startEmailShareActivity() {
		Intent emailIntent = PNShareIntentsHandler.getInstance().getIntent(PNShareIntentsHandler.PNIntent.EMAIL);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "This is the subject");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "This is the text");
        startActivityForResult(emailIntent, ACTIVITY_EMAIL_SHARE);
	}
	
	/**
	 * Starts Gmail share Activity
	 */
	private void startGmailShareActivity() {
		Intent gmailIntent = PNShareIntentsHandler.getInstance().getIntent(PNShareIntentsHandler.PNIntent.GMAIL);
		startActivityForResult(gmailIntent, ACTIVITY_GMAIL_SHARE);
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
    			Uri selectedImage = intent.getData();
				String[] filePathColumn = {MediaStore.Images.Media.DATA};
				
				Cursor cursor = getContentResolver().query(selectedImage, 
					filePathColumn, null, null, null);
				startManagingCursor(cursor);
				cursor.moveToFirst();
				
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				mImgFilePath = cursor.getString(columnIndex);
				
				Log.v("PN", "Selected image file path: "+mImgFilePath);
				
				setNoteImageToView(mImgFilePath);
    		}
    		else {
    			Log.w("PN", "ACTIVITY_IMG_GALLERY returned: "+resultCode);
    		}
    		break;
    	case ACTIVITY_EMAIL_SHARE:
    		if( resultCode == RESULT_OK)
    			Log.v("PN", "email share activity OK");
    		else
    			Log.v("PN", "email share not ok: "+resultCode);
    	case ACTIVITY_GMAIL_SHARE:
    		if( resultCode == RESULT_OK)
    			Log.v("PN", "Gmail share activity OK");
    		else
    			Log.v("PN", "Gmail share not ok: "+resultCode);
    	}
    }
	
	/**
	 * Displays selected image, increases the ImageView size, and sets the remove
	 * image icon to now be visible.
	 * @param strFilePath file path to the image to display
	 */
	private void setNoteImageToView(String strFilePath) {
		Bitmap selectedImg = BitmapFactory.decodeFile(strFilePath);
		if( mImgView != null ) {
			//TODO: place the values in a resource file instead of using magic numbers
			float scale = getResources().getDisplayMetrics().density;
			float newWidth = 120.f * scale;
			float newHeight = 120.f * scale;

			LayoutParams frame = mImgView.getLayoutParams();
			frame.width = (int)newWidth;
			frame.height = (int)newHeight;
			mImgView.setLayoutParams(frame);
			
			mImgView.setImageBitmap(selectedImg);
		}
		
		if( mRemoveImgIcon != null ) {
			mRemoveImgIcon.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * Called when checkbox is toggled on or off
	 * @param bIsChecked
	 */
	private void onNoteIsPrayedFor(boolean bIsChecked) {
		if( bIsChecked ) {
			// Get current time to be saved to database
			if( mDbAdapter != null )
				mDatePrayed = mDbAdapter.getCurrentDateForDb();
			
			// Display this time to screen
			if( mPrayedForStatus != null )
				mPrayedForStatus.setText(
						getResources().getText(R.string.last_prayed_for).toString()
						+ mDbAdapter.convertDbDateToString(mDatePrayed));
			
			// Change text for checkbox
			if( mPrayedForCheckBox != null )
				mPrayedForCheckBox.setText(R.string.checkbox_prayed_success_text);
		}
		else {
			// Reset info if checkbox is unchecked
			mDatePrayed = 0;
			
			if( mPrayedForStatus != null )
				mPrayedForStatus.setText(
					getResources().getText(R.string.last_prayed_for).toString()
					+ getResources().getText(R.string.date_never).toString());
			
			if( mPrayedForCheckBox != null )
				mPrayedForCheckBox.setText(R.string.checkbox_prayed_for_text);
		}
	}
	
	/**
	 * Removes note image from being displayed and nulls out file path that will
	 * updated when state is saved
	 */
	private void removeNoteImage() {
		// Null out file path.  WIll be saved to 
		mImgFilePath = null;
		
		// Revert image display
		if( mImgView != null ) {
			mImgView.setImageResource(R.drawable.img_gallery);
			
			//TODO: place the values in a resource file instead of using magic numbers
			float scale = getResources().getDisplayMetrics().density;
			float newWidth = 80.f * scale;
			float newHeight = 80.f * scale;
			
			LayoutParams frame = mImgView.getLayoutParams();
			frame.width = (int)newWidth;
			frame.height = (int)newHeight;
			mImgView.setLayoutParams(frame);
		}
		
		if( mRemoveImgIcon != null ) {
			mRemoveImgIcon.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * Create dialog box prompting user to confirm delete action.
	 * showDialog() will take care to display it.
	 */
	private AlertDialog createDeleteDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.dialog_delete_confirm)
			.setCancelable(false)
			.setPositiveButton(R.string.dialog_yes,	new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Delete note from database
					if( mDbAdapter != null && mDbRowId != null ) {
						mDbAdapter.deleteNote(mDbRowId);
					}
					
					// Exit the Activity
					setResult(RESULT_OK);
					finish();
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
	 * Create dialog box with list of options for user to share the note
	 */
	private AlertDialog createShareDialog() {
		// Determine if Activity is available before 
		// List of share options
		final CharSequence[] items = getAvailableShareActivities();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_share_title);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				Log.v("SHARE", "clicked item:"+item);
				switch(item) {
				case 0:
					startEmailShareActivity();
					break;
				case 1:
					startGmailShareActivity();
					break;
				}
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
		case DIALOG_SHARE_NOTE:
			return createShareDialog();
		}

		return super.onCreateDialog(id);
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

	/**
	 *  Called when Activity is being stopped and put in background state
	 *  @param outState Bundle that can be used to save any dynamic instance state
	 *         Can be retrieved when Activity is started again in onCreate()
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.v("SAVE", "onSaveInstanceState - mDbRowId="+mDbRowId);
		outState.putSerializable(PNDbAdapter.PNKEY_ROWID, mDbRowId);
	}
}

