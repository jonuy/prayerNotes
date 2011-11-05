/**
 * PNViewAdapter
 * 
 * Custom adapter used to display the info in our ListViews
 */

package com.jdroyd.prayerNotes;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class PNViewAdapter extends SimpleAdapter {
	
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
			// Instead of simply using super.setViewImage(), we decode the file
			// first to a smaller sample size that will use less memory and
			// hopefully prevent us from seeing the OOM crash for large images
			int req_size = 0;
			if( v != null ) {
				LayoutParams frame = v.getLayoutParams();
				int w = frame.width;
				int h = frame.height;
				req_size = w > h ? w : h;
			}
			v.setImageBitmap(decodeFile(value, req_size));
			
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
	
	/**
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
}
