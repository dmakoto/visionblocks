package edu.mit.cameraCulture.vblocks.predefined;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;

public class RemoteVideo extends Module{
	
	public static final String REGISTER_SERVICE_NAME = "RemoteVideoBlock"; 
	private RemoteCameraView surfaceView;
	private byte [] mCurrentFrame;
	private int mFrameWidth;
	private int mFrameHeight;
	
	//private MediaMetadataRetriever retriever = new MediaMetadataRetriever();
	  
	//private String url = "http://18.85.55.108:8080/webcam"; 
	//private String url = "http://10.189.3.119:8080/webcam"; 
	private String url = Environment.getExternalStorageDirectory().getPath() + "/vid/HOTEI.mp4";
	//private String url = "http://foodcam.media.mit.edu/axis-cgi/mjpg/video.cgi";
    private Bitmap vImg;
    
	
	private MediaPlayer player;
	
	public RemoteVideo(){
		super(REGISTER_SERVICE_NAME);
	}
	
	@Override
	public void onCreate(EngineActivity context){
		
		super.onCreate(context);
		
		surfaceView = new RemoteCameraView(context);		
		context.getLayout().addView(surfaceView);
		
		player = new MediaPlayer();
		
//		retriever = new MediaMetadataRetriever();
//		retriever.setDataSource(url); 		
		
		try {
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setDataSource(url);
			//player.setOnPreparedListener(this);
			//player.prepareAsync(); // might take long! (for buffering, etc)
			player.prepare();
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		player.start();
		
		
	}
	
//	public void onPrepared(MediaPlayer player) {
//		player.start();
//	}
	
	@Override
	public void onDestroyModule(){
		mContext.getLayout().removeView(surfaceView);
		player.release();
		player = null;
		super.onDestroyModule();	
	}
	
	@Override
	public ExecutionCode execute(Sample image) {
//		synchronized(lock) {
//			try {
//				lock.wait();
//				image.setImageData(mCurrentFrame, mFrameWidth, mFrameHeight);
//			} catch (InterruptedException e) {
//				Log.e(REGISTER_SERVICE_NAME,e.getMessage());
//			}
//		}
//		
//		int currentPosition = player.getCurrentPosition(); //in millisecond
//
//	    Bitmap bmFrame = retriever.getFrameAtTime(currentPosition * 1000); 
//		

		
		//vImg = surfaceView.getDrawingCache();

//		Canvas can = surfaceView.getHolder().lockCanvas();
//		if(can != null){
//			surfaceView.draw(can);
//			surfaceView.getHolder().unlockCanvasAndPost(can);
//		}
		
				
		if((vImg != null) && (vImg.getWidth() != 0)){
			synchronized(vImg){
//				try {					
					//vImg.wait();
				
					//Log.d("debug", "2 debug:" + vImg.getWidth() + " , " + vImg.getHeight());
				
					int bytes = vImg.getByteCount();
					ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
					vImg.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
	
					mCurrentFrame = buffer.array(); 
					mFrameWidth = vImg.getWidth();
					mFrameHeight = vImg.getHeight();
					
					//TODO: convert to YUV?
					
					image.setImageData(mCurrentFrame, mFrameWidth, mFrameHeight);
					
//				} catch (InterruptedException e) {
//					Log.e(REGISTER_SERVICE_NAME,e.getMessage());
//				}
			}
		}
		
		surfaceView.postInvalidate();
		
		
		return ExecutionCode.NONE;
	}
	
	public class RemoteCameraView extends SurfaceView implements SurfaceHolder.Callback  {
		
		public static final String TAG = "FILTER_PREVIEW"; 
	 	private SurfaceHolder mHolder;
	    
	    private Paint paint;

		
		 public RemoteCameraView(Context context) {
		        super(context);
		        mHolder = getHolder();
		        
		        //vImg = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);	
		        
		        
 
//				Canvas can;
//				try {
//					 can = mHolder.lockCanvas();
//					 
//				     can.setBitmap(vImg);	
//				     mHolder.unlockCanvasAndPost(can);
//				     
//				} catch (IllegalArgumentException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} 
		        
		        
		        mHolder.addCallback(this);		        
		        
		        
		        paint = new Paint();
		 }
		 
		 public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			 
			 vImg = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			 
		 }
		 
		 public void surfaceDestroyed(SurfaceHolder holder) {
			 
	    }
		 
		 public void surfaceCreated(SurfaceHolder holder) {
		
			 mHolder = holder;		 
			 
			 //setDrawingCacheEnabled(true);
			 
			 vImg = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);			
//			 
//			 Canvas can;
//			try {
//				 can = mHolder.lockCanvas();
//				 
//			     can.setBitmap(vImg);	
//			     mHolder.unlockCanvasAndPost(can);
//			     
//			} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
			 
			 Log.d("debug", "" + url);
			 
			 player.setDisplay(mHolder);	 
 
		 }
		 
		 
		 
		 public void draw(Canvas canvas){
			 
			   Log.d("debug", "1 debug");
			 
			 	if(vImg == null) vImg = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Config.ARGB_8888);
			 	
			 	canvas.setBitmap(vImg);
			 	
		        super.draw(canvas);
		        
		        paint.setARGB(255, 255, 0, 0);
		        canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 50, paint);	  
		        
		 }
		
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Remote Video";
	}
	
	public static String getModuleName(){
		return "Remote Video";
	}

	//@Override
	public int describeContents() {
		return 0;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
	}

}
