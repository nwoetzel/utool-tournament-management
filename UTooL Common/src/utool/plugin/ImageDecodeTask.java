package utool.plugin;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * Task responsible for loading an image without requiring the application to wait for its completion.
 * @author Justin Kreier
 * @version 3/10/2013
 */
public class ImageDecodeTask extends AsyncTask<Player, Void, Bitmap> {

	/**
	 * The image view to apply the bitmap to
	 */
	public ImageView v;

	/**
	 * Constructor which holds a reference to the image view to apply the image to
	 * @param iv The image view to apply the bitmap to
	 */
	public ImageDecodeTask(ImageView iv) {
		v = iv;
	}

	protected Bitmap doInBackground(Player... params) {

		if (params == null || params.length == 0){
			return null;
		} else {
			Bitmap bitmap = null;
			if(isCancelled()) {
				return bitmap;
			}
			
			bitmap = params[0].getPortrait();
			
			return bitmap;
		}
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if(v != null) {
			if (result != null){
				v.setImageBitmap(result);
				
			}
		}
	}

}