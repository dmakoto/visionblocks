package edu.mit.cameraCulture.vblocks.predefined;

//import org.mozilla.javascript.Context;
//import org.mozilla.javascript.Scriptable;

import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;
import android.content.Intent;
import android.media.ExifInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GrayscaleJavascript extends Module {
	private static String GRAYSCALE_METHOD = "var grayscale = function () { "
			+ "		var data = image;"
			+ "		var i, r, g, b, v;"
			+ "		for (i=0, o=0; i<100000; i+=4,o++) {"
			+ "			r = data[i];"
			+ "			g = data[i+1];"
			+ "			b = data[i+2];"
			+ "			v = 0.2126*r + 0.7152*g + 0.0722*b;"
			+ "			intImage[o] = 0xff000000 | (v<<16) & 0xff0000 | (v<<8) & 0xff00 | v & 0xff;"
			+ "		}" + "};";

	// private Scriptable scope;
	// private Context cx;

	public static final String REGISTER_SERVICE_NAME = "GrayscaleJavascript";

	public GrayscaleJavascript() {
		super(REGISTER_SERVICE_NAME);
		// // cx = Context.enter();
		// try
		// {
		// scope = cx.initStandardObjects();
		// cx.evaluateString( scope, GRAYSCALE_METHOD, "Filter", 1, null );
		// }
		// catch( Exception e )
		// {
		// e.printStackTrace();
		// Log.d("RHINO_TEST",e.getMessage() );
		// }
		// finally
		// {
		// // Exit the Context. This removes the association between the Context
		// and the current thread and is an
		// // essential cleanup action. There should be a call to exit for every
		// call to enter.
		// // Context.exit();
		// }
	}

	@Override
	public ExecutionCode execute(Sample image) {
		// scope.put("image", scope, Context.toObject(image.getImageData(),
		// scope));
		// scope.put("intImage", scope, Context.toObject(imageData, scope));
		// Log.d("Javascript","Entering ...");
		// cx.evaluateString(scope, "grayscale()", "run", 1, null);
		// Log.d("Javascript","Finished");
		return ExecutionCode.NONE;
	}

	@Override
	public String getName() {
		return getModuleName();
	}

	public static String getModuleName() {
		return "Grayscale (Javascript)";
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

	public static final Parcelable.Creator<GrayscaleJavascript> CREATOR = new Parcelable.Creator<GrayscaleJavascript>() {
		public GrayscaleJavascript createFromParcel(Parcel in) {
			Log.v("ParcelableTest", "Creating from parcel");
			return new GrayscaleJavascript();
		}

		public GrayscaleJavascript[] newArray(int size) {
			return new GrayscaleJavascript[size];
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
