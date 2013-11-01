package edu.mit.cameraCulture.vblocks;

import java.util.HashMap;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.mit.cameraCulture.vblocks.Module.PixelEncoding;

import android.graphics.PixelFormat;
import android.util.Log;

/**
 * Representation of an Image. It has the basic properties
 * such as width, height, image data array, and matrices
 * referent to the sample.
 * Sample will get information as a stream of bytes in YUV format
 * and always keep a Mat (from OpenCV) representation in YUV and RGB
 * format.
 * @author CameraCulture
 */
public class Sample {
	private HashMap<String, Mat> mSateliteMats;
	private HashMap<String, Scalar> mSateliteScalars;
	
	private int mWidth;
	private int mHeight;
	private byte[] mImageData;
	
	private Mat mYUV_Mat;
	private Mat mRgb_Mat;
	
	private PixelEncoding mPixelFormat;
	
	/**
	 * Creates a Sample object.
	 * @param data Stream of bytes representing a image in YUV format (Android camera default)
	 * @param width The width of the image represented by data. width is not zero.
	 * @param height The height of the image represented by data. width is not zero.
	 */
//	public Sample(byte [] data, int width, int height){
//		mWidth = width;
//		mHeight = height;
//		mImageData = data;
//		mSateliteMats = new HashMap<String, Mat>();
//		mSateliteScalars = new HashMap<String, Scalar>();
//		
//		// Convert data to Mat
//		mYUV_Mat = new Mat(mHeight + mHeight / 2, mWidth, CvType.CV_8UC1);
//		mRgb_Mat = new Mat(mHeight, mWidth, CvType.CV_8UC4 );
//	}
	
	/**
	 * Creates a Sample object, that still doesn't have any information.
	 * Requires that the image information is set after, by calling setImageData()
	 */
	public Sample(){
		mWidth = 0;
		mHeight = 0;
		mSateliteMats = new HashMap<String, Mat>();
		mSateliteScalars = new HashMap<String, Scalar>();
		mImageData = null;
		mRgb_Mat = null;
		mYUV_Mat = null;
	}
	
	public Mat getMat(String matName){
		return mSateliteMats.containsKey(matName)?mSateliteMats.get(matName):null;
	}
	
	public void setMat(String matName, Mat m){
		if(!mSateliteMats.containsKey(matName)){
			mSateliteMats.put(matName, m);
		}
	}
	
	public void removeMat(String matName){
		if(mSateliteMats.containsKey(matName)){
			mSateliteMats.remove(matName);
		}
	}
	
	
	public Scalar getScalar(String scalarName){
		return mSateliteScalars.containsKey(scalarName)?mSateliteScalars.get(scalarName):null;
	}
	
	/**
	 * Get the image data in a stream of bytes.
	 * @return the raw information that was set in the last setImageData()
	 */
	public byte[] getImageData() {
		return mImageData;
	}

	/**
	 * Set the Sample information with the information of the image passed as imageData.
	 * @param imageData YUV format representation of the image (Android camera default)
	 * @param width The width of the image
	 * @param height The height of the image
	 */
	public void setImageData(byte [] imageData, int width, int height) {
		this.mImageData = imageData;
		this.mWidth = width;
		this.mHeight = height;
		
		// Initialize Mats
		mYUV_Mat = new Mat(mHeight + mHeight / 2, mWidth, CvType.CV_8UC1);
		mRgb_Mat = new Mat(mHeight, mWidth, CvType.CV_8UC4 );
		
		// Populates Mat with information
		mYUV_Mat.put(0, 0, imageData);
		
		// Creates the RGB Mat from YUV Mat using OpenCV
		Imgproc.cvtColor(mYUV_Mat, mRgb_Mat, Imgproc.COLOR_YUV420sp2RGB, 4);
	}
	
	public int getWidth() {
		return mRgb_Mat.cols();
	}
	
	public int getHeight() {
		return mRgb_Mat.rows();
	}
	
	public Mat getRgbMat() {
		return mRgb_Mat;
	}
	
	public Mat getYUVMat() {
		return mYUV_Mat;
	}
}
