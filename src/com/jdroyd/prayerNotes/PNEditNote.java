package com.jdroyd.prayerNotes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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
	public static final int ACTIVITY_SHARE = 4;
	
	private static final int DIALOG_DELETE_NOTE = 0;
	private static final int DIALOG_CANCEL_EDIT = 1;
	private static final int DIALOG_SET_ALARM = 2;
	
	private static final int MENU_CANCEL_ID = 0;
	
	private PNDbAdapter mDbAdapter;
	private EditText mNoteText;
	private Long mDbRowId;
	
	// Handles to UI elements
	private Button mSaveButton;
	private Button mShareButton;
	private Button mDiscardButton;
	private Button mAlarmButton;
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
	
	// Cache original values so we can check if any edits have been made later
	private String mNoteText_Original;
	private String mImgFilePath_Original;
	private int mDatePrayed_Original;
	
	// Dialog used to set reminder
	private PNAlarmSetter mAlarmSetter;
	
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
		mAlarmButton = (Button)findViewById(R.id.edit_note_alarm);
		mImgView = (ImageView)findViewById(R.id.edit_note_img);
		mRemoveImgIcon = (ImageView)findViewById(R.id.edit_note_img_remove);
		mPrayedForCheckBox = (CheckBox)findViewById(R.id.edit_note_prayedFor);
		mPrayedForStatus = (TextView)findViewById(R.id.edit_note_prayedFor_status);

		mSaveButton.setOnClickListener(this);
		mShareButton.setOnClickListener(this);
		mDiscardButton.setOnClickListener(this);
		mAlarmButton.setOnClickListener(this);
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
		
		// Cache original values.  Must be done after populateFields() since
		// that gets and populates the initial data from the Cursor
		cacheOriginalNoteValues();
	}
	
	/**
	 * Cache original values so we can compare later to determine if edits
	 * have been made
	 */
	private void cacheOriginalNoteValues() {
		if( mNoteText != null ) {
			mNoteText_Original = mNoteText.getText().toString();
		}
		mImgFilePath_Original = mImgFilePath;
		mDatePrayed_Original = mDatePrayed;
	}
	
	/**
     * Manually called in handled cases where we want to exit that Activity
     */
    private void exitActivity() {
    	setResult(RESULT_OK);
		finish();
    }
	
	/**
     * Add to the Options Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	menu.add(0, MENU_CANCEL_ID, 0, R.string.edit_menu_cancel);
    	return true;
    }
    
    /**
     * Handle behavior when an options menu item is selected
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch( item.getItemId() ) {
    	case MENU_CANCEL_ID:
    		cancelActivityRequest();
    		return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    /**
     * Handle Back button presses for pre-2.0 versions
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            // Take care of calling this method on earlier versions (pre-2.0) of
            // the platform where it doesn't exist.
            onBackPressed();
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Handle behavior when Back button is pressed
     */
    @Override
    public void onBackPressed() {
        cancelActivityRequest();
        return;
    }
    
    /**
     * Behavior of Activity when user presses Back button or Cancel from menu
     *  - check if any changes were made, get confirmation from user to abandon
     *  changes, and then cancel Activity
     */
    private void cancelActivityRequest() {
    	if( isNoteChanged() ) {
    		// Confirm player wants to abandon changes before exiting
    		showDialog(DIALOG_CANCEL_EDIT);
    	}
    	else {
    		exitActivity();
    	}
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
	 * Determines if note has been edited or not
	 * @return true if edited, false otherwise
	 */
	private boolean isNoteChanged() {
		if( mNoteText != null ) {
			String currText = mNoteText.getText().toString();
			// One string may be null and the other is not
			if( (currText != null && mNoteText_Original == null) || 
				(currText == null && mNoteText_Original != null) )
				return true;
			
			// Check if text has changed
			if( currText != null && mNoteText_Original != null ) {
				if( currText.compareTo(mNoteText_Original) != 0 )
					return true;
			}
		}
		
		// One may be null and the other is not
		if( (mImgFilePath_Original != null && mImgFilePath == null) ||
			(mImgFilePath_Original == null && mImgFilePath != null) ) {
			return true;
		}
		
		// Check if selected images have changed
		if( mImgFilePath != null && mImgFilePath_Original != null ) {
			if( mImgFilePath.compareTo(mImgFilePath_Original) != 0 )
				return true;
		}
		
		// Check if date prayed has changed
		if( mDatePrayed_Original != mDatePrayed ) {
			return true;
		}
		
		return false;
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
			startShareActivity();
			break;
		case R.id.edit_note_discard:
			if( isNewNote() )
				exitActivity();
			else
				showDialog(DIALOG_DELETE_NOTE);
			break;
		case R.id.edit_note_alarm:
				showDialog(DIALOG_SET_ALARM);
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
		i.setType("image/*");
		startActivityForResult(i, ACTIVITY_IMG_GALLERY);
	}
	
	/**
	 * Creates Intent and starts Activity to send/share the note
	 */
	private void startShareActivity() {
		Intent i=new Intent(android.content.Intent.ACTION_SEND);
		i.setType("text/plain");
		
		// Subject set to: "prayerNote from <current date>"
		String date = "";
		if( mDbAdapter != null) {
			// Use date note was created for subject line
			if( mDateCreated > 0 )
				date = mDbAdapter.convertDbDateToString(mDateCreated);
			// Otherwise, use the current date
			else
				date = mDbAdapter.convertDbDateToString(mDbAdapter.getCurrentDateForDb());
		}
		
		String subject = getResources().getText(R.string.app_name) +
			" from " + date;
		i.putExtra(Intent.EXTRA_SUBJECT, subject);
		
		// Get current text on screen, not what's saved in the database (could be old)
		String text = mNoteText.getText().toString();
		i.putExtra(Intent.EXTRA_TEXT, text);
		
		// Attach image to the intent.  Change Type
		if( mImgFilePath != null ) {
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mImgFilePath)));
			i.setType("image/*");
		}
		
		// Use createChooser() to allow user to select application
		Intent chooser = Intent.createChooser(i, 
				getResources().getText(R.string.dialog_share_title));
		startActivityForResult(chooser, ACTIVITY_SHARE);
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
    	case ACTIVITY_SHARE:
    		if( resultCode == RESULT_OK)
    			Log.v("PN", "share activity OK");
    		else
    			Log.v("PN", "share not ok: "+resultCode);
    		break;
    	}
    }
	
	/**
	 * Displays selected image, increases the ImageView size, and sets the remove
	 * image icon to now be visible.
	 * @param strFilePath file path to the image to display
	 */
	private void setNoteImageToView(String strFilePath) {
		if( mImgView != null ) {
			//TODO: place the values in a resource file instead of using magic numbers
			float scale = getResources().getDisplayMetrics().density;
			float newWidth = 120.f * scale;
			float newHeight = 120.f * scale;

			LayoutParams frame = mImgView.getLayoutParams();
			frame.width = (int)newWidth;
			frame.height = (int)newHeight;
			mImgView.setLayoutParams(frame);
			
			int req_size = frame.width > frame.height ? frame.width : frame.height;
			Bitmap selectedImg = decodeFile(strFilePath, req_size);
			
			mImgView.setImageBitmap(selectedImg);
		}
		
		if( mRemoveImgIcon != null ) {
			mRemoveImgIcon.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * TODO: This is the same as what's in PrayerNotes class.  Consolidate?
	 * Returns a Bitmap that's a subsample of the original image.  Allows
	 * us to retrieve an image that requires less memory and can help us
	 * avoid the OOM exceptions.
	 */
	private Bitmap decodeFile(String filePath, int req_size){
		Bitmap img = null;
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
		
			FileInputStream fis = new FileInputStream(filePath);
			BitmapFactory.decodeStream(fis, null, o);
			fis.close();

			int scale = 1;
			if (o.outHeight > req_size || o.outWidth > req_size) {
				scale = (int)Math.pow(2, (int) Math.round(Math.log(req_size 
						/ (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
			}
		
			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			fis = new FileInputStream(filePath);
			img = BitmapFactory.decodeStream(fis, null, o2);
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.e("PN", "Image file not found during decoding: "+filePath);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("PN", "Image IOException during decoding: "+filePath);
		}
		return img;
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
					exitActivity();
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
	 * Create dialog box prompting user to confirm abandoning any edits made.
	 */
	private AlertDialog createCancelEditDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.dialog_cancel_edit)
			.setCancelable(false)
			.setPositiveButton(R.string.dialog_yes,	new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					exitActivity();
				}
			})
			.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
		
		AlertDialog alert = builder.create();
		return alert;
	}
	
	/**
	 * Create dialog box for user to set alarm reminder.
	 */
	private Dialog createSetAlarmDialog() {
		mAlarmSetter = new PNAlarmSetter(this);
		mAlarmSetter.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				String txt1="", txt2="";
				if( mAlarmSetter != null ) {
					txt1 = mAlarmSetter.getTime();
					txt2 = mAlarmSetter.getDate();
				}
				Log.v("PN", "dialog has been dismissed - "+txt1+" "+txt2);
			}
		});
		return mAlarmSetter;
	}
	
	/**
	 * Called first time showDialog() is called for a given id
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_DELETE_NOTE:
			return createDeleteDialog();
		case DIALOG_CANCEL_EDIT:
			return createCancelEditDialog();
		case DIALOG_SET_ALARM:
			return createSetAlarmDialog();
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

