package edu.mit.cameraCulture.vblocks.predefined;

import java.util.Random;
import java.util.Vector;

import org.opencv.core.Point;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.googlecode.tesseract.android.TessBaseAPI;

import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.R;
import edu.mit.cameraCulture.vblocks.Sample;

public class LetterBasedGrader extends Module {

	public static final String REGISTER_SERVICE_NAME = "LetterBasedGrader";

	private static final String TESSBASE_PATH = "/mnt/sdcard/";
	private static final String DEFAULT_LANGUAGE = "eng";
	private static final String EXPECTED_FILE = TESSBASE_PATH + "tessdata/"
			+ DEFAULT_LANGUAGE;

	private TessBaseAPI baseApi;

	private int[] image8888;
	private int imgWidth;
	private int imgHeight;

	private Vector<String> teacherAnswers;
	private Vector<String> studentAnwsers;
	private int studentGrade;

	private LetterGraderView view;

	private String recognizedText;

	public LetterBasedGrader() {
		super(REGISTER_SERVICE_NAME);
	}

	public ExecutionCode execute(Sample image) {

		if (image8888 == null) {
			imgWidth = image.getWidth();
			imgHeight = image.getHeight();
			image8888 = new int[imgWidth * imgHeight];

		}
		synchronized (image8888) {
			decodeYUV420SP(image8888, image.getImageData(), imgWidth, imgHeight);
		}

		view.postInvalidate();

		return null;

	}

	public String getName() {
		return "Letter Grader";
	}

	public static String getModuleName() {
		return "Letter Grader";
	}

	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}

	public void onCreate(EngineActivity context) {

		super.onCreate(context);

		baseApi = new TessBaseAPI();
		baseApi.init(TESSBASE_PATH, DEFAULT_LANGUAGE);

		Log.d("OCR", "OCR Libraly loaded");

		recognizedText = "";

		teacherAnswers = new Vector<String>();
		studentAnwsers = new Vector<String>();

		view = new LetterGraderView(context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		context.getLayout().addView(view, lp);
	}

	public static final Parcelable.Creator<LetterBasedGrader> CREATOR = new Parcelable.Creator<LetterBasedGrader>() {
		public LetterBasedGrader createFromParcel(Parcel in) {
			Log.v("ParcelableTest", "Creating from parcel");
			return new LetterBasedGrader();
		}

		public LetterBasedGrader[] newArray(int size) {
			return new LetterBasedGrader[size];
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

	class LetterGraderView extends View {

		private int appState = 0;

		private Vector<Rect> questions;
		private Vector<Rect> screenQs;
		private Vector<Integer> qColors;

		private Vector<Point> q;

		private Bitmap back;

		private Rect ackButtom;
		private Rect invAckButtom;
		private int ackBColor;
		private Rect subButtom;
		private int nextBColor;

		private Random rand;
		private int displayW;
		private Rect screenSize;
		private Rect imageSize;

		private int touchState = 0;
		private int qState = 0;

		private Bitmap nextButton;
		private Rect nextBImgSize;
		private Bitmap qButton;
		private Rect qBImgSize;
		private Bitmap checkButton;
		private Rect checkBImgSize;
		private Bitmap reButton;
		private Rect reBImgSize;

		private Paint paint;

		public LetterGraderView(Context context) {
			super(context);
			this.setOnTouchListener(new LetterGraderOnTouch());

			rand = new Random();

			displayW = getWidth();
			paint = new Paint();

			questions = new Vector<Rect>();
			questions.add(new Rect());

			screenQs = new Vector<Rect>();
			screenQs.add(new Rect());

			qColors = new Vector<Integer>();
			qColors.add(new Integer(Color.rgb(rand.nextInt(255),
					rand.nextInt(255), rand.nextInt(255))));

			imageSize = new Rect();

			nextButton = BitmapFactory.decodeResource(getResources(),
					R.drawable.b_next);
			nextBImgSize = new Rect(0, 0, nextButton.getWidth(),
					nextButton.getHeight());
			qButton = BitmapFactory.decodeResource(getResources(),
					R.drawable.b_q);
			qBImgSize = new Rect(0, 0, qButton.getWidth(), qButton.getHeight());
			checkButton = BitmapFactory.decodeResource(getResources(),
					R.drawable.b_check);
			checkBImgSize = new Rect(0, 0, checkButton.getWidth(),
					checkButton.getHeight());
			reButton = BitmapFactory.decodeResource(getResources(),
					R.drawable.b_re);
			reBImgSize = new Rect(0, 0, reButton.getWidth(),
					reButton.getHeight());

		}

		protected void onDraw(Canvas canvas) {

			if (displayW != getWidth()) {
				int width = this.getWidth();
				int height = this.getHeight();

				ackButtom = new Rect(width - 120, height - 120, width, height);
				invAckButtom = new Rect(width - 120, height, width - 240,
						height - 120);
				ackBColor = Color.rgb(250, 0, 0);
				subButtom = new Rect(width - 240, height - 120, width - 120,
						height);
				nextBColor = Color.rgb(0, 250, 0);
				screenSize = new Rect(0, 0, width, height);

			}
			if (appState == 0) {

				paint.setARGB(255, 255, 0, 0);
				paint.setStrokeWidth(2);
				paint.setStyle(Style.FILL);
				paint.setTextSize(40);
				canvas.drawText("capture teacher's answer", 5, 55, paint);

				// paint.setColor(ackBColor);
				paint.setStyle(Style.FILL);
				// canvas.drawRect(ackButtom, paint);
				paint.setARGB(255, 255, 255, 255);
				canvas.drawBitmap(nextButton, nextBImgSize, ackButtom, paint);
			} else if (appState == 1) {

				paint.setARGB(255, 255, 255, 255);
				canvas.drawBitmap(back, imageSize, screenSize, paint);

				// paint.setColor(ackBColor);
				paint.setStyle(Style.FILL);
				// canvas.drawRect(ackButtom, paint);
				paint.setARGB(255, 255, 255, 255);
				canvas.drawBitmap(nextButton, nextBImgSize, ackButtom, paint);

				// paint.setColor(nextBColor);
				paint.setStyle(Style.FILL);
				if (qState == 0) {
					// canvas.drawRect(subButtom, paint);
					canvas.drawBitmap(qButton, qBImgSize, subButtom, paint);
				}

				paint.setARGB(255, 255, 0, 0);
				paint.setStrokeWidth(2);
				paint.setStyle(Style.FILL);
				paint.setTextSize(40);
				canvas.drawText("Select the answer positions", 5, 55, paint);
				canvas.drawText("number of questions: " + questions.size(), 5,
						100, paint);

				drawQs(canvas);
			} else if (appState == 2) {

				// if(teacherAnswers.size() == questions.size()){
				// paint.setColor(ackBColor);
				paint.setStyle(Style.FILL);
				// canvas.drawRect(ackButtom, paint);
				paint.setARGB(255, 255, 255, 255);
				canvas.drawBitmap(nextButton, nextBImgSize, ackButtom, paint);
				// }

				// paint.setColor(nextBColor);
				paint.setStyle(Style.FILL);
				// canvas.drawRect(subButtom, paint);
				paint.setARGB(255, 255, 255, 255);
				canvas.drawBitmap(reButton, reBImgSize, subButtom, paint);

				paint.setARGB(255, 255, 0, 0);
				paint.setStrokeWidth(2);
				paint.setStyle(Style.FILL);
				paint.setTextSize(40);
				canvas.drawText(
						"Confirm teacher's answer or re-do de detection", 5,
						55, paint);

				drawQs(canvas);

			} else if (appState == 3) {

				// paint.setColor(ackBColor);
				paint.setStyle(Style.FILL);
				// canvas.drawRect(ackButtom, paint);
				paint.setARGB(255, 255, 255, 255);
				canvas.drawBitmap(checkButton, checkBImgSize, ackButtom, paint);

				drawQs(canvas);
				if (studentAnwsers.size() == questions.size())
					drawGraderRes(canvas);
			}

			// this.postInvalidateDelayed(30);

		}

		private void drawQs(Canvas canvas) {

			for (int i = 0; i < questions.size(); i++) {

				paint.setColor(qColors.get(i).intValue());
				paint.setStyle(Style.STROKE);
				paint.setStrokeWidth(2);
				canvas.drawRect(screenQs.get(i), paint);

				if (teacherAnswers.size() == questions.size()) {
					paint.setTextSize(40);
					paint.setStyle(Style.FILL);
					canvas.drawText(teacherAnswers.get(i),
							screenQs.get(i).left, screenQs.get(i).top, paint);
				}

			}

		}

		private void drawGraderRes(Canvas canvas) {

			for (int i = 0; i < questions.size(); i++) {

				if (!studentAnwsers.get(i).equals(teacherAnswers.get(i))) {
					paint.setARGB(255, 255, 0, 0);
					paint.setStrokeWidth(6);
					paint.setStyle(Style.STROKE);
					canvas.drawLine(screenQs.get(i).left, screenQs.get(i).top,
							screenQs.get(i).right, screenQs.get(i).bottom,
							paint);
					canvas.drawLine(screenQs.get(i).right, screenQs.get(i).top,
							screenQs.get(i).left, screenQs.get(i).bottom, paint);
				} else {
					paint.setARGB(255, 0, 255, 0);
					paint.setStrokeWidth(6);
					paint.setStyle(Style.STROKE);
					canvas.drawCircle(
							(screenQs.get(i).right + screenQs.get(i).left) / 2,
							(screenQs.get(i).top + screenQs.get(i).bottom) / 2,
							Math.abs(screenQs.get(i).right
									- screenQs.get(i).left) / 2, paint);
				}

				paint.setTextSize(40);
				paint.setStyle(Style.FILL);
				canvas.drawText(studentAnwsers.get(i), screenQs.get(i).right,
						screenQs.get(i).top, paint);

				String gradeString = "" + studentGrade + "|" + questions.size();
				paint.setARGB(255, 255, 0, 0);
				paint.setStrokeWidth(3);
				paint.setStyle(Style.FILL);
				paint.setTextSize(100);
				canvas.drawText(gradeString, 5, 105, paint);
			}
		}

		void getTeacherAnswer(Bitmap img) {
			String res;

			baseApi.setImage(img);
			teacherAnswers.clear();

			for (int i = 0; i < questions.size(); i++) {

				questions.get(i).sort();
				screenQs.get(i).sort();

				baseApi.setRectangle(questions.get(i));
				baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR);
				res = baseApi.getUTF8Text();

				// quick fix!!
				res = bruteForceCorrection(res);

				teacherAnswers.add(res);

			}

			baseApi.clear();
		}

		void getStudentAnswer() {

			String res;
			Bitmap img = Bitmap.createBitmap(image8888, imgWidth, imgHeight,
					Bitmap.Config.ARGB_8888);
			baseApi.setImage(img);
			studentAnwsers.clear();
			studentGrade = 0;

			for (int i = 0; i < questions.size(); i++) {

				baseApi.setRectangle(questions.get(i));
				baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR);
				res = baseApi.getUTF8Text();

				// quick fix!!
				res = bruteForceCorrection(res);

				studentAnwsers.add(res);

				if (res.equals(teacherAnswers.get(i)))
					studentGrade++;

			}
			baseApi.clear();

		}

		private String bruteForceCorrection(String res) {

			// quick fix!!!!!
			if (!res.equals("A") && !res.equals("C") && !res.equals("D")
					&& !res.equals("E")) {
				// strong errors
				if (res.equals("/"))
					res = "B";
				else if (res.equals("\\"))
					res = "A";
				else if (res.equals(":"))
					res = "B";
				else if (res.equals(";"))
					res = "B";
				else if (res.equals("."))
					res = "B";
				else if (res.equals("%"))
					res = "C";

				// week errors
				else if (res.equals("n"))
					res = "A";
				else if (res.equals("3"))
					res = "B";
				else if (res.equals("W"))
					res = "B";

				// last case senario
				else
					res = "B";
			}

			return res;
		}

		class LetterGraderOnTouch implements OnTouchListener {

			public boolean onTouch(View v, MotionEvent event) {

				int imgX = (int) (imgWidth * (1.0 * event.getX() / getWidth()));
				int imgY = (int) (imgHeight * (1.0 * event.getY() / getHeight()));

				int x = (int) event.getX();
				int y = (int) event.getY();

				if (appState == 0) {
					if (ackButtom.contains(x, y)) {
						back = Bitmap.createBitmap(image8888, imgWidth,
								imgHeight, Bitmap.Config.ARGB_8888);

						imageSize.left = 0;
						imageSize.top = 0;
						imageSize.right = imgWidth;
						imageSize.bottom = imgHeight;

						appState = 1;
						view.postInvalidate();

						qState = 1;

					}
				} else if (appState == 1) {

					if (event.getAction() == MotionEvent.ACTION_DOWN) {

						if (ackButtom.contains(x, y)) {

							if (qState == 1) {
								Log.d("grader", "q last removed");
								questions.remove(questions.size() - 1);
								qColors.remove(qColors.size() - 1);
								screenQs.remove(screenQs.size() - 1);

							}

							getTeacherAnswer(back);
							appState = 2;

							view.postInvalidate();

						} else if (subButtom.contains(x, y)) {
							if (qState == 0) {
								Rect rec = new Rect();
								Rect srec = new Rect();
								questions.add(rec);
								screenQs.add(srec);
								qColors.add(new Integer(Color.rgb(
										rand.nextInt(255), rand.nextInt(255),
										rand.nextInt(255))));
								view.postInvalidate();
								qState = 1;
							}
						} else {

							questions.get(questions.size() - 1).left = imgX;
							questions.get(questions.size() - 1).top = imgY;
							screenQs.get(screenQs.size() - 1).left = x;
							screenQs.get(screenQs.size() - 1).top = y;
							questions.get(questions.size() - 1).right = imgX;
							questions.get(questions.size() - 1).bottom = imgY;
							screenQs.get(screenQs.size() - 1).right = x;
							screenQs.get(screenQs.size() - 1).bottom = y;

							touchState = 1;
							qState = 0;
							view.postInvalidate();

						}
					} else if (event.getAction() == MotionEvent.ACTION_MOVE) {

						questions.get(questions.size() - 1).right = imgX;
						questions.get(questions.size() - 1).bottom = imgY;
						screenQs.get(screenQs.size() - 1).right = x;
						screenQs.get(screenQs.size() - 1).bottom = y;
						touchState = 0;
						// view.postInvalidate();
					}
					// }

					// }

				} else if (appState == 2) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (ackButtom.contains(x, y)) {
							// getTeacherAnswer();
							if (teacherAnswers.size() == questions.size()) {
								appState = 3;
								view.postInvalidate();
							}
						} else if (subButtom.contains(x, y)) {

							Bitmap img = Bitmap.createBitmap(image8888,
									imgWidth, imgHeight,
									Bitmap.Config.ARGB_8888);
							getTeacherAnswer(img);

						}
					}
				} else if (appState == 3) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (ackButtom.contains(x, y))
							getStudentAnswer();
						view.postInvalidate();
					}

				}

				return true;
			}
		}

	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		Log.d("TOUCH", this.toString());
		return false;
	}

}
