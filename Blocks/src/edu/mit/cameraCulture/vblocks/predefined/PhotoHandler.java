package edu.mit.cameraCulture.vblocks.predefined;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import edu.mit.cameraCulture.vblocks.utils.Storage;

public class PhotoHandler implements PictureCallback {

  private final Context context;

  public PhotoHandler(Context context) {
    this.context = context;
  }

  @Override
  public void onPictureTaken(byte[] data, Camera camera) {
	  Storage mStorage = new Storage(context);
	  mStorage.save(data);
  }
}