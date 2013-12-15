package edu.mit.cameraCulture.vblocks.predefined;

import java.util.Random;
import java.util.Vector;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.ModuleTouchListener;
import edu.mit.cameraCulture.vblocks.Sample;

public class Grader extends Module {

	public static final String REGISTER_SERVICE_NAME = "Grader";

	private int appState;
	private ModuleTouchListener listener;

	private Mat baseMatBW;
	private Mat tempMat;

	private int[] image8888;
	private int imgWidth;
	private int imgHeight;

	// vector<questions < fill boxes < box corners > > >
	private Vector<Vector<Vector<Point>>> questions;

	private Vector<Integer> teacherAnsw;
	private Vector<Integer> studentAnsw;

	private Random rand;

	private int histBase[];
	private int histTarg[];

	private int studentGrade;

	private GraderView view;

	public Grader() {
		super(REGISTER_SERVICE_NAME);

	}

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

	@Override
	public ExecutionCode execute(Sample image) {
		// convert image to RGB
		if (image8888 == null) {
			imgWidth = image.getWidth();
			imgHeight = image.getHeight();
			image8888 = new int[imgWidth * imgHeight];

		}
		synchronized (image8888) {
			decodeYUV420SP(image8888, image.getImageData(), imgWidth, imgHeight);
		}

		view.postInvalidate();
		// Log.d("Grader","executed");

		return null;
	}
	
	@Override
	public ModuleTouchListener getModuleTouchListener() {
		return listener;
	}

	public void onCreate(EngineActivity context) {
		super.onCreate(context);

		tempMat = new Mat();
		baseMatBW = new Mat();

		questions = new Vector<Vector<Vector<Point>>>();

		teacherAnsw = new Vector<Integer>();
		studentAnsw = new Vector<Integer>();

		studentGrade = -1;

		rand = new Random();
		histBase = new int[2];
		histTarg = new int[2];

		appState = 0;

		view = new GraderView(context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		context.getLayout().addView(view, lp);

	}

	@Override
	public void onDestroyModule() {
		super.onDestroyModule();

	}

	@Override
	public String getName() {
		return "Grader";
	}

	public static String getModuleName() {
		return "Grader";
	}

	@Override
	protected void onHandleIntent(Intent intent) {

	}

	public static final Parcelable.Creator<Grader> CREATOR = new Parcelable.Creator<Grader>() {
		public Grader createFromParcel(Parcel in) {
			Log.v("ParcelableTest", "Creating from parcel");
			return new Grader();
		}

		public Grader[] newArray(int size) {
			return new Grader[size];
		}

	};

	private float calHistogramDiff(int[] histBase, int[] histTarg, float divVal) {

		float val = (float) Math.sqrt((histBase[0] - histTarg[0])
				* (histBase[0] - histTarg[0]) + (histBase[1] - histTarg[1])
				* (histBase[1] - histTarg[1]));

		return (val / divVal);

	}

	private float getHist(Mat bwMat, int[] hist, Vector<Point> sqPoints) {

		int i0 = 0;
		int i1 = 0;

		int minX = (int) sqPoints.get(0).x;
		if (minX > sqPoints.get(1).x)
			minX = (int) sqPoints.get(1).x;

		int maxX = (int) sqPoints.get(0).x;
		if (maxX < sqPoints.get(1).x)
			maxX = (int) sqPoints.get(1).x;

		int minY = (int) sqPoints.get(0).y;
		if (minY > sqPoints.get(1).y)
			minY = (int) sqPoints.get(1).y;

		int maxY = (int) sqPoints.get(0).y;
		if (maxY < sqPoints.get(1).y)
			maxY = (int) sqPoints.get(1).y;

		for (int i = minX; i < maxX; i++) {
			for (int j = minY; j < maxY; j++) {

				if (bwMat.get(j, i)[0] > 50.0)
					i1++;
				else
					i0++;

			}
		}

		hist[0] = i0;
		hist[1] = i1;

		return (maxX - minX) * (maxY - minY);

	}

	private int getStudentGrade(Mat img) {

		float qVal = -1;
		int maxQ = 0;
		float maxQVal = -1;

		int grade = 0;

		studentAnsw.clear();

		for (int i = 0; i < questions.size(); i++) {

			Vector<Vector<Point>> ques = questions.get(i);

			maxQVal = -1;
			qVal = -1;
			maxQ = 0;

			for (int j = 0; j < ques.size(); j++) {

				float rectArea = 1.0f * getHist(img, histTarg, ques.get(j));
				getHist(baseMatBW, histBase, ques.get(j));

				qVal = calHistogramDiff(histBase, histTarg, rectArea);

				if (qVal > maxQVal) {
					maxQ = j;
					maxQVal = qVal;
				}

			}

			studentAnsw.add(new Integer(maxQ));
			if (maxQ == teacherAnsw.get(i).intValue())
				grade++;

			// teacherAnsw.add(new Integer(maxQ));

		}

		Log.d("grader", "student grade = " + grade);

		return grade;

	}

	private void getTeacherAnsw(Mat img) {

		float qVal = -1;
		int maxQ = 0;
		float maxQVal = -1;

		teacherAnsw.clear();

		for (int i = 0; i < questions.size(); i++) {

			Vector<Vector<Point>> ques = questions.get(i);

			maxQVal = -1;
			qVal = -1;
			maxQ = 0;

			for (int j = 0; j < ques.size(); j++) {

				float rectArea = 1.0f * getHist(img, histTarg, ques.get(j));
				getHist(baseMatBW, histBase, ques.get(j));

				qVal = calHistogramDiff(histBase, histTarg, rectArea);

				if (qVal > maxQVal) {
					maxQ = j;
					maxQVal = qVal;
				}

			}

			teacherAnsw.add(new Integer(maxQ));

		}

	}

	class GraderView extends View {

		private Vector<Integer> qColors;

		private int displayW;

		// button corners
		private Vector<Point> actionButton;
		private int actBColor;

		private Vector<Point> nextQbutton;
		private int nQBColor;

		private Point center;

		private Point lineP1;
		private Point lineP2;

		private int gScalar;
		private int rScalar;

		private Point textPos;

		private Rect imgSize;
		private Rect screenSize;

		// for touch interaction
		private Vector<Point> q;

		private Bitmap background;

		private Paint paint;

		private Bitmap bkCopy;

		private int touchState;

		public GraderView(Context context) {
			super(context);

			displayW = getWidth();

			qColors = new Vector<Integer>();

			actBColor = Color.rgb(255, 0, 0);

			nQBColor = Color.rgb(0, 255, 0);

			center = new Point();
			lineP1 = new Point();
			lineP2 = new Point();
			rScalar = Color.rgb(250, 0, 0);
			gScalar = Color.rgb(0, 250, 0);

			textPos = new Point(0, 50);

			q = new Vector<Point>();

			paint = new Paint();

			touchState = 0;

			listener = new ModuleTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {

					int imgX = (int) (imgWidth * (1.0 * event.getX() / getWidth()));
					int imgY = (int) (imgHeight * (1.0 * event.getY() / getHeight()));

					int x = (int) event.getX();
					int y = (int) event.getY();

					Log.d("Grader", "touch = " + x + "," + y + " of "
							+ imgWidth + "," + imgHeight);

					if (appState == 0) {
						if (insideButton(actionButton, x, y)) {

							if (image8888 != null) {

								imgSize = new Rect(0, 0, imgWidth, imgHeight);

								background = Bitmap.createBitmap(image8888,
										imgWidth, imgHeight,
										Bitmap.Config.ARGB_8888);
								Mat gray = new Mat();
								Mat img = new Mat();
								Utils.bitmapToMat(background, img);
								Imgproc.cvtColor(img, gray,
										Imgproc.COLOR_RGB2GRAY);
								Imgproc.threshold(gray, baseMatBW, 5, 200,
										Imgproc.THRESH_BINARY
												| Imgproc.THRESH_OTSU);

								questions.add(new Vector<Vector<Point>>());
								qColors.add(new Integer(Color.rgb(
										rand.nextInt(255), rand.nextInt(255),
										rand.nextInt(255))));
								appState = 1;
								Log.d("Grader", "to state 1");
								view.postInvalidate();
							}
						}
					} else if (appState == 1) {

						if (((event.getAction() == MotionEvent.ACTION_DOWN) || (event
								.getAction() == MotionEvent.ACTION_POINTER_DOWN))) {
							if (insideButton(actionButton, x, y)) {
								appState = 2;
								view.postInvalidate();
							} else if (insideButton(nextQbutton, x, y)) {

								questions.add(new Vector<Vector<Point>>());
								qColors.add(new Integer(Color.rgb(
										rand.nextInt(255), rand.nextInt(255),
										rand.nextInt(255))));

								Log.d("Grader", "new Q created");
								view.postInvalidate();
							} else {

								if (touchState == 0) {
									touchState = 1;
									q = new Vector<Point>();
									q.add(new Point(imgX, imgY));
								} else {
									touchState = 0;
									q.add(new Point(imgX, imgY));
									Vector<Vector<Point>> lastQ = questions
											.get(questions.size() - 1);
									lastQ.add(q);
									Log.d("Grader", "new field created");
								}
								view.postInvalidate();
							}
						}

					} else if (appState == 2) {
						if (insideButton(actionButton, x, y)
								&& (teacherAnsw.size() == questions.size())) {
							appState = 3;
							view.postInvalidate();
						} else if (insideButton(nextQbutton, x, y)) {

							background = Bitmap.createBitmap(image8888,
									imgWidth, imgHeight,
									Bitmap.Config.ARGB_8888);
							Mat gray = new Mat();
							Mat img = new Mat();
							Mat frameBW = new Mat();
							Utils.bitmapToMat(background, img);
							Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGB2GRAY);
							Imgproc.threshold(gray, frameBW, 5, 200,
									Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

							getTeacherAnsw(frameBW);

							String anwsS = "(";
							for (int i = 0; i < questions.size(); i++)
								anwsS += (teacherAnsw.get(i).intValue() + 1)
										+ ",";
							anwsS += ")";

							Log.d("Grader", anwsS);
							view.postInvalidate();

						}
					} else if (appState == 3) {

						if (insideButton(actionButton, x, y)
								&& (teacherAnsw.size() == questions.size())) {

							background = Bitmap.createBitmap(image8888,
									imgWidth, imgHeight,
									Bitmap.Config.ARGB_8888);
							Mat gray = new Mat();
							Mat img = new Mat();
							Mat frameBW = new Mat();
							Utils.bitmapToMat(background, img);
							Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGB2GRAY);
							Imgproc.threshold(gray, frameBW, 5, 200,
									Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

							studentGrade = getStudentGrade(frameBW);
							view.postInvalidate();

						}
					}

					return true;
				}

				@Override
				public boolean onSingleTapConfirmed(MotionEvent arg0) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean onDoubleTapEvent(MotionEvent arg0) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean onDoubleTap(MotionEvent arg0) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub

				}
			};

			this.setOnTouchListener(listener);

		}

		boolean insideButton(Vector<Point> corners, float x, float y) {
			if ((x > corners.get(0).x) && (x < corners.get(1).x)
					&& (y > corners.get(0).y) && (y < corners.get(1).y))
				return true;
			else
				return false;
		}

		protected void onDraw(Canvas canvas) {

			// Log.d("Grader","draw");

			// if((image8888 != null) && (background == null)) background =
			// Bitmap.createBitmap(image8888, imgWidth, imgHeight,
			// Bitmap.Config.ARGB_8888);
			// if(background != null) background.setPixels(image8888, 0, 1, 0,
			// 0, imgWidth, imgHeight);
			//
			if (displayW != getWidth()) {

				int width = this.getWidth();
				int height = this.getHeight();

				actionButton = new Vector<Point>();
				actionButton.add(new Point(width - 80, height - 80));
				actionButton.add(new Point(width, height));

				nextQbutton = new Vector<Point>();
				nextQbutton.add(new Point(width - 160, height - 80));
				nextQbutton.add(new Point(width - 80, height));

				screenSize = new Rect(0, 0, width, height);

			}

			// if(image8888 != null) background = Bitmap.createBitmap(image8888,
			// imgWidth, imgHeight, Bitmap.Config.ARGB_8888);

			paint.setARGB(255, 255, 255, 255);

			if (appState == 0) {
				if (image8888 != null) {
					// canvas.drawBitmap(background, 0, 0, paint);
					paint.setColor(actBColor);
					paint.setStyle(Style.FILL);

					paint.setColor(rScalar);
					paint.setStrokeWidth(3);
					paint.setTextSize(10);
					canvas.drawText("Capture the blank sheet",
							(float) textPos.x, (float) textPos.y, paint);

					canvas.drawRect((float) actionButton.get(0).x,
							(float) actionButton.get(0).y,
							(float) actionButton.get(1).x,
							(float) actionButton.get(1).y, paint);
					// canvas.drawRect(0, 0, 80, 80,paint);
				}
			} else if (appState == 1) {

				canvas.drawBitmap(background, imgSize, screenSize, paint);

				paint.setStyle(Style.FILL);
				paint.setColor(actBColor);
				canvas.drawRect((float) actionButton.get(0).x,
						(float) actionButton.get(0).y,
						(float) actionButton.get(1).x,
						(float) actionButton.get(1).y, paint);
				paint.setColor(nQBColor);
				canvas.drawRect((float) nextQbutton.get(0).x,
						(float) nextQbutton.get(0).y,
						(float) nextQbutton.get(1).x,
						(float) nextQbutton.get(1).y, paint);

				paint.setColor(rScalar);
				paint.setStrokeWidth(3);
				paint.setTextSize(25);
				canvas.drawText("select the checkbox positions",
						(float) textPos.x, (float) textPos.y, paint);

				// draw the rects;
				drawQs(canvas);

			} else if (appState == 2) {
				paint.setStyle(Style.FILL);
				paint.setColor(actBColor);
				if (teacherAnsw.size() > 0)
					canvas.drawRect((float) actionButton.get(0).x,
							(float) actionButton.get(0).y,
							(float) actionButton.get(1).x,
							(float) actionButton.get(1).y, paint);
				paint.setColor(nQBColor);
				canvas.drawRect((float) nextQbutton.get(0).x,
						(float) nextQbutton.get(0).y,
						(float) nextQbutton.get(1).x,
						(float) nextQbutton.get(1).y, paint);

				paint.setColor(rScalar);
				paint.setStrokeWidth(3);
				paint.setTextSize(25);
				canvas.drawText("Capture teachers answers", (float) textPos.x,
						(float) textPos.y, paint);

				// draw the rects;
				drawQs(canvas);
			} else if (appState == 3) {

				paint.setStyle(Style.FILL);
				paint.setColor(actBColor);
				canvas.drawRect((float) actionButton.get(0).x,
						(float) actionButton.get(0).y,
						(float) actionButton.get(1).x,
						(float) actionButton.get(1).y, paint);

				drawQs(canvas);
				if (studentAnsw.size() == questions.size())
					drawStudentRes(canvas);

			}

			// this.postInvalidateDelayed(60);

		}

		private void drawStudentRes(Canvas canvas) {

			float aspectX = (1.0f * screenSize.right) / (1.0f * imgWidth);
			float aspectY = (1.0f * screenSize.bottom) / (1.0f * imgHeight);

			Vector<Point> field;

			paint.setStyle(Style.STROKE);

			for (int i = 0; i < questions.size(); i++) {

				field = questions.get(i).get(studentAnsw.get(i).intValue());

				if (studentAnsw.get(i).intValue() == teacherAnsw.get(i)
						.intValue()) {
					paint.setColor(gScalar);
					paint.setStrokeWidth(6);
					float centerX = (float) (aspectX * field.get(0).x + aspectX
							* field.get(1).x) / 2.0f;
					float centerY = (float) (aspectY * field.get(0).y + aspectY
							* field.get(1).y) / 2.0f;
					float radius = (float) (1.15f * (Math.abs(aspectX
							* field.get(0).x - aspectX * field.get(1).x) / 2.0f));
					canvas.drawCircle(centerX, centerY, radius, paint);
				} else {

					lineP1.x = aspectX * field.get(0).x;
					lineP1.y = aspectY * field.get(1).y;

					lineP2.x = aspectX * field.get(1).x;
					lineP2.y = aspectY * field.get(0).y;

					paint.setColor(rScalar);
					paint.setStrokeWidth(6);
					canvas.drawLine((float) (aspectX * field.get(0).x),
							(float) (aspectY * field.get(0).y),
							(float) (aspectX * field.get(1).x),
							(float) (aspectY * field.get(1).y), paint);
					canvas.drawLine((float) lineP1.x, (float) lineP1.y,
							(float) lineP2.x, (float) lineP2.y, paint);

				}

			}

			String gradeString = "" + studentGrade + "|" + questions.size();
			paint.setColor(rScalar);
			paint.setStrokeWidth(3);
			paint.setTextSize(50);
			canvas.drawText(gradeString, (float) textPos.x, (float) textPos.y,
					paint);

		}

		private void drawQs(Canvas canvas) {

			float aspectX = (1.0f * screenSize.right) / (1.0f * imgWidth);
			float aspectY = (1.0f * screenSize.bottom) / (1.0f * imgHeight);

			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(2);

			for (int i = 0; i < questions.size(); i++) {

				paint.setColor(qColors.get(i));

				Vector<Vector<Point>> questFields = questions.get(i);

				for (int j = 0; j < questFields.size(); j++) {

					Vector<Point> field = questFields.get(j);

					if (field.size() > 1) {
						if ((teacherAnsw.size() > 0)) {
							if (teacherAnsw.get(i) != j) {
								canvas.drawRect(
										(float) (aspectX * field.get(0).x),
										(aspectY * (float) field.get(0).y),
										(float) (aspectX * field.get(1).x),
										(float) (aspectY * field.get(1).y),
										paint);
							} else {
								float centerX = (float) (aspectX
										* field.get(0).x + aspectX
										* field.get(1).x) / 2.0f;
								float centerY = (float) (aspectY
										* field.get(0).y + aspectY
										* field.get(1).y) / 2.0f;
								float radius = (float) Math.abs(aspectX
										* field.get(0).x - aspectX
										* field.get(1).x) / 2.0f;
								canvas.drawCircle(centerX, centerY, radius,
										paint);
							}
						} else
							canvas.drawRect((float) (aspectX * field.get(0).x),
									(aspectY * (float) field.get(0).y),
									(float) (aspectX * field.get(1).x),
									(float) (aspectY * field.get(1).y), paint);
					}
				}
			}
		}
	}
}
