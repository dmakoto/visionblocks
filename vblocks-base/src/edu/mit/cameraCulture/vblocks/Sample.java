package edu.mit.cameraCulture.vblocks;

import java.util.HashMap;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import edu.mit.cameraCulture.vblocks.Module.PixelEncoding;

import android.graphics.PixelFormat;

/**
 * Representation of an Image. It has the basic properties
 * such as width, height, image data array, and matrices
 * referent to the sample.
 * @author CameraCulture
 */
public class Sample {
	private HashMap<String, Mat> mSateliteMats;
	private HashMap<String, Scalar> mSateliteScalars;
	
	private byte [] mImageData;
	private int mWidth;
	private int mHeight;
	
	private PixelEncoding mPixelFormat;
	
	
	public Sample(byte [] data, int width, int height){
		mWidth = width;
		mHeight = height;
		mImageData = data;
		mSateliteMats = new HashMap<String, Mat>();
		mSateliteScalars = new HashMap<String, Scalar>();
	}
	
	public Sample(){
		mWidth = 0;
		mHeight = 0;
		mImageData = null;
		mSateliteMats = new HashMap<String, Mat>();
		mSateliteScalars = new HashMap<String, Scalar>();
	}
	
	public byte [] getImageData() {
		return mImageData;
	}
	
	public Mat getMat(String matName){
		return mSateliteMats.containsKey(matName)?mSateliteMats.get(matName):null;
	}
	
	public void setMat(String matName, Mat m){
		if(!mSateliteMats.containsKey(matName)){
			mSateliteMats.put(matName, m);
		}
	}
	
	
	public Scalar getScalar(String scalarName){
		return mSateliteScalars.containsKey(scalarName)?mSateliteScalars.get(scalarName):null;
	}

	public void setImageData(byte [] imageData, int width, int height) {
		this.mImageData = imageData;
		this.mWidth = width;
		this.mHeight = height;
	}
	
	public int getWidth() {
		return mWidth;
	}
	
	public int getHeight() {
		return mHeight;
	}
	
}
