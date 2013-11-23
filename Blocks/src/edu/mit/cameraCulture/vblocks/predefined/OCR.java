package edu.mit.cameraCulture.vblocks.predefined;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.predefined.Grader.GraderView;

public class OCR extends Module {

	public static final String REGISTER_SERVICE_NAME = "OCR";

	private static final String TESSBASE_PATH = Environment
			.getExternalStorageDirectory().getPath();// "/mnt/sdcard/"; //
														// hardcoding is
														// dangerouse, carefull
	private static final String DEFAULT_LANGUAGE = "eng";
	// This was unused, so I commented it out
	// private static final String EXPECTED_FILE = TESSBASE_PATH + "tessdata/" +
	// DEFAULT_LANGUAGE;

	private TessBaseAPI baseApi;

	private int[] image8888;
	private int imgWidth;
	private int imgHeight;

	private OCRView view;

	private String recognizedText = "";

	private int detectionMode = -1;

	public OCR() {
		super(REGISTER_SERVICE_NAME);
	}

	@Override
	public ExecutionCode execute(Sample image) {

		if (image8888 == null) {
			imgWidth = image.getWidth();
			imgHeight = image.getHeight();
			image8888 = new int[imgWidth * imgHeight];

		}
		synchronized (image8888) {
			decodeYUV420SP(image8888, image.getImageData(), imgWidth, imgHeight);
		}

		return null;

	}

	@Override
	public String getName() {
		return "Text Reader";
	}

	public static String getModuleName() {
		return "Text Reader";
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}

	public void onCreate(EngineActivity context) {

		super.onCreate(context);

		baseApi = new TessBaseAPI();
		baseApi.init(TESSBASE_PATH, DEFAULT_LANGUAGE);

		Log.d("OCR", "OCR Libraly loaded");

		recognizedText = "";

		view = new OCRView(context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		context.getLayout().addView(view, lp);

	}

	public static final Parcelable.Creator<OCR> CREATOR = new Parcelable.Creator<OCR>() {
		public OCR createFromParcel(Parcel in) {
			Log.v("ParcelableTest", "Creating from parcel");
			return new OCR();
		}

		public OCR[] newArray(int size) {
			return new OCR[size];
		}

	};

	private static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width,
			int height) {
		final int frameSize = width * height;

		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}
				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}

	class OCRView extends View {

		int initX;
		int initY;
		int endX;
		int endY;

		int initImgX;
		int initImgY;
		int endImgX;
		int endImgY;

		Rect wordRect;

		Paint paint;

		public OCRView(Context context) {
			super(context);
			this.setOnTouchListener(new OCROnTouch());

			wordRect = new Rect();
			paint = new Paint();

		}

		protected void onDraw(Canvas canvas) {

			paint.setStyle(Style.STROKE);
			paint.setARGB(255, 255, 0, 0);
			canvas.drawRect(initX, initY, endX, endY, paint);

			paint.setStyle(Style.FILL);
			paint.setTextSize(20);
			canvas.drawText(recognizedText, 5, 25, paint);

			view.postInvalidateDelayed(30);

		}

		class OCROnTouch implements OnTouchListener {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				int imgX = (int) (imgWidth * (1.0 * event.getX() / getWidth()));
				int imgY = (int) (imgHeight * (1.0 * event.getY() / getHeight()));

				int x = (int) event.getX();
				int y = (int) event.getY();

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if ((x > initX) && (y > initY) && (x < endX) && (y < endY)) {

						wordRect.left = initImgX;
						wordRect.top = initImgY;
						wordRect.right = endImgX;
						wordRect.bottom = endImgY;

						Bitmap img = Bitmap.createBitmap(image8888, imgWidth,
								imgHeight, Bitmap.Config.ARGB_8888);
						baseApi.setImage(img);
						baseApi.setRectangle(wordRect);
						if (detectionMode > 0)
							baseApi.setPageSegMode(detectionMode);
						recognizedText = baseApi.getUTF8Text();
						baseApi.clear();

						Log.d("OCR", "Words detected = " + recognizedText);

					} else {
						initX = x;
						initImgX = imgX;
						initY = y;
						initImgY = imgY;
					}

				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					endX = x;
					endImgX = imgX;
					endY = y;
					endImgY = imgY;
				}

				return true;
			}

		}

	}
}
