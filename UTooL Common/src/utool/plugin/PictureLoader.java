package utool.plugin;

import java.io.File;
import java.io.IOException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.util.Log;

/**
 * Helper class for loading and orienting images taken with the Android camera
 * @author Justin Kreier
 * @version 3/17/2013
 */
public class PictureLoader {
	
	/**
	 * Loads a picture from the file system
	 * @param filepath The image to load
	 * @return The loaded image as a bitmap
	 */
	public static Bitmap loadAndOrientPicture(String filepath){
		int rotate = 0;
		try{
			File imageFile = new File(filepath);
			if(!imageFile.exists()){
				//if it doesn't exist, we can return right now
				return null;
			}
			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,  ExifInterface.ORIENTATION_NORMAL);

			switch(orientation){
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			}
		} catch(IOException e){
			Log.e("PortraitOrientation", "Orientation detection threw an error", e);
		}

		Bitmap bm = null;
		Bitmap scaled = null;
		Bitmap rotated = null;

		bm = BitmapFactory.decodeFile(filepath);
		if (bm != null){
			scaled = Bitmap.createScaledBitmap(bm, 120, 120, false);
			
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);
			rotated = Bitmap.createBitmap(scaled, 0,0,scaled.getWidth(), scaled.getHeight(), matrix, true);
			
		}

		if (bm != null){
			bm.recycle();
		}
		if (scaled != null){
			scaled.recycle();
		}

		return rotated;
	}
	
	/**
	 * Loads a picture from the file system
	 * @param r The system resources to use
	 * @param filepath The image to load
	 * @return The loaded image as a bitmap drawable
	 */
	public static BitmapDrawable loadAndOrientPicture(Resources r, String filepath){
		int rotate = 0;
		try{
			File imageFile = new File(filepath);
			if(!imageFile.exists()){
				//if it doesn't exist, we can return right now
				return null;
			}
			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,  ExifInterface.ORIENTATION_NORMAL);

			switch(orientation){
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			}
		} catch(IOException e){
			Log.e("PortraitOrientation", "Orientation detection threw an error", e);
		}

		Bitmap bm = null;
		Bitmap scaled = null;
		Bitmap rotated = null;

		bm = BitmapFactory.decodeFile(filepath);
		if (bm != null){
			scaled = Bitmap.createScaledBitmap(bm, 120, 120, false);
			
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);
			rotated = Bitmap.createBitmap(scaled, 0,0,scaled.getWidth(), scaled.getHeight(), matrix, true);
		}

		if (bm != null){
			bm.recycle();
		}
		if (scaled != null){
			scaled.recycle();
		}

		return new BitmapDrawable(r, rotated);
	}

}
