package edu.mit.cameraCulture.vblocks.predefined;

import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GrayscaleNative extends Module {
	public static final String REGISTER_SERVICE_NAME = "GrayscaleNative";

	public static native int ConvertToGrayscale(byte[] byteData,
			int[] imageData, int width, int height);

	public GrayscaleNative() {
		super(REGISTER_SERVICE_NAME);
	}

	static {
		System.loadLibrary("opencv_java");
		System.loadLibrary("QCAR");
		System.loadLibrary("vision_blocks");
	}

	@Override
	public String getName() {
		return getModuleName();
	}

	public static String getModuleName() {
		return "Grayscale (Native)";
	}

	@Override
	public ExecutionCode execute(Sample image) {
		// ConvertToGrayscale(image.getImageData(), imageData, image.getWidth(),
		// image.getHeight());
		return ExecutionCode.NONE;
	}

	// @Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	// @Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub

	}

	public static final Parcelable.Creator<GrayscaleNative> CREATOR = new Parcelable.Creator<GrayscaleNative>() {
		public GrayscaleNative createFromParcel(Parcel in) {
			Log.v("ParcelableTest", "Creating from parcel");
			return new GrayscaleNative();
		}

		public GrayscaleNative[] newArray(int size) {
			return new GrayscaleNative[size];
		}
	};

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		Log.d("TOUCH", this.toString());
		return false;
	}
}
