package edu.mit.cameraCulture.vblocks.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Storage {

	private final Context context;

	public Storage(Context context) {
		this.context = context;
	}
	
	public void save(byte[] data) {
		File pictureFileDir = getDir();

		if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

			Log.d("TAKEPICTURE", "Can't create directory to save image.");
			((Activity)context).runOnUiThread(new Runnable() {
				public void run(){
					Toast.makeText(context, "Can't create directory to save image.",
							Toast.LENGTH_LONG).show();
				}
			});
			
			return;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
		String date = dateFormat.format(new Date());
		final String photoFile = "Picture_" + date + ".jpg";

		String filename = pictureFileDir.getPath() + File.separator + photoFile;

		File pictureFile = new File(filename);

		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
			((Activity)context).runOnUiThread(new Runnable() {
				public void run(){
					Toast.makeText(context, "New Image saved:" + photoFile,
							Toast.LENGTH_LONG).show();
				}
			});
			
		} catch (Exception error) {
			Log.d("TAKEPICTURE", "File" + filename + "not saved: "
					+ error.getMessage());
			((Activity)context).runOnUiThread(new Runnable() {
				public void run(){
					Toast.makeText(context, "Image could not be saved.",
							Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	private File getDir() {
		File sdDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		return new File(sdDir, "VisionBlocks");
	}
}