package edu.mit.cameraCulture.vblocks.predefined;

import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GrayscaleJava extends Module {

	public static final String REGISTER_SERVICE_NAME = "GrayscaleJava";

	public GrayscaleJava() {
		super(REGISTER_SERVICE_NAME);
	}

	@Override
	public void onCreate(EngineActivity context) {
		super.onCreate(context);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public ExecutionCode execute(Sample image) {
		byte[] data = image.getImageData();
		for (int i = 0, o = 0; i < data.length; i += 4, o++) {
			byte r = data[i];
			byte g = data[i + 1];
			byte b = data[i + 2];
			byte v = (byte) (0.2126 * r + 0.7152 * g + 0.0722 * b);
			// imageData[o] = 0xff000000 | (v<<16) & 0xff0000 | (v<<8) & 0xff00
			// | v & 0xff;
		}
		return ExecutionCode.NONE;
	}

	@Override
	public String getName() {
		return getModuleName();
	}

	public static String getModuleName() {
		return "Grayscale (Java)";
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

	public static final Parcelable.Creator<GrayscaleJava> CREATOR = new Parcelable.Creator<GrayscaleJava>() {
		public GrayscaleJava createFromParcel(Parcel in) {
			Log.v("ParcelableTest", "Creating from parcel");
			return new GrayscaleJava();
		}

		public GrayscaleJava[] newArray(int size) {
			return new GrayscaleJava[size];
		}

	};

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}
}
