package edu.mit.cameraCulture.vblocks.predefined;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import edu.mit.cameraCulture.vblocks.CommitableView;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;

public class Blur extends Module {

	public static final String REGISTER_SERVICE_NAME = "Blur";

	private Size kSize;
	private int blurSize = 20;

	private Mat mRgb_Mat;

	public Blur() {
		super(REGISTER_SERVICE_NAME);

	}

	@Override
	public ExecutionCode execute(Sample image) {

		kSize.height = blurSize;
		kSize.width = blurSize;

		mRgb_Mat = image.getRgbMat();

		synchronized (mRgb_Mat) {
			// Apply blur
			Imgproc.blur(mRgb_Mat, mRgb_Mat, kSize);

			System.gc();

		}

		return null;
	}

	@Override
	public String getName() {
		return "Blur";
	}

	public static String getModuleName() {
		return "Blur";
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCreate(EngineActivity context) {

		super.onCreate(context);

		kSize = new Size(20, 20);
	}

	@Override
	public CommitableView getConfigurationView(Context context) {
		return new BlurConfig(context, this);
	}

	public int getBlurSize() {
		return blurSize;
	}

	public void setBlurSize(int size) {
		blurSize = size;
	}

	@Override
	public void onDestroyModule() {
		super.onDestroyModule();
	}

	public static final Parcelable.Creator<Blur> CREATOR = new Parcelable.Creator<Blur>() {
		public Blur createFromParcel(Parcel in) {
			Log.v("ParcelableTest", "Creating from parcel");
			return new Blur();
		}

		public Blur[] newArray(int size) {
			return new Blur[size];
		}

	};
}
