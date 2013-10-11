/*
 * OpticalFlow.cpp
 *
 *  Created on: 14.3.2013
 *      Author: bubo
 */


#include <android/log.h>
#include "OpticalFlow.h"

bool isOpticalFlowInitialized = false;
OpticalFlow * opticalFlow = 0;

JNIEXPORT jint JNICALL
		Java_edu_mit_cameraCulture_vblocks_predefined_OpticalFlow_processImage(
				JNIEnv* env,
				jobject thiz,
				jbyteArray source,
				jint width,
				jint height,
				jfloatArray jrect){
	jboolean isCopy;
	jbyte * source_bytes = env->GetByteArrayElements(source, &isCopy);
	char * source_array = reinterpret_cast<char *>(source_bytes);

	jboolean isRectCopy;
	jfloat * rect_array = env->GetFloatArrayElements(jrect, &isRectCopy);
	float * rect = reinterpret_cast<float *>(rect_array);

	if(!isOpticalFlowInitialized){
		opticalFlow = new OpticalFlow(width,height);
		isOpticalFlowInitialized = true;
	} else if(opticalFlow == 0){
		env->ReleaseFloatArrayElements(jrect,rect_array,0);
		env->ReleaseByteArrayElements(source,source_bytes,0);
		return 0;
	}

	opticalFlow->findFlow(source_array,width,height,rect);
	env->ReleaseFloatArrayElements(jrect,rect_array,0);
	env->ReleaseByteArrayElements(source,source_bytes,0);
	return 42;
}

JNIEXPORT jint JNICALL
			Java_edu_mit_cameraCulture_vblocks_predefined_OpticalFlow_clean(
					JNIEnv* env,
					jobject thiz){
	delete opticalFlow;
	opticalFlow = 0;
	isOpticalFlowInitialized = false;
	fcvCleanUp();
	return 0;
}


OpticalFlow::OpticalFlow(int width, int height) {
	width = width/2;
	height = height/2;


	int size = width * height;
	featureXY_in = new float[size*2];
	featureXY_out = new float[size*2];
	featureStatus_in = new int32_t[size];

	for(int i = 7; i > 0; i--){
		int levels = (int) pow(2.0,i);
		if((width % levels) == 0 && (height % levels) == 0)
		{
			nPyramidLevels =  i+1;
			__android_log_print(ANDROID_LOG_INFO,"OpticalFlow","Setting nPyramidLevels to: %d ", nPyramidLevels);
			break;
		}

	}

	__android_log_print(ANDROID_LOG_INFO,"OpticalFlow","Creating pyramid8 width: %d  height: %d nLevels: %d ", width, height, nPyramidLevels);

	src1Pyr = new fcvPyramidLevel[nPyramidLevels];
	src2Pyr = new fcvPyramidLevel[nPyramidLevels];
	dx1Pyr = new fcvPyramidLevel[nPyramidLevels];
	dy1Pyr = new fcvPyramidLevel[nPyramidLevels];

	fcvPyramidAllocate( src1Pyr, width, height, 4, nPyramidLevels, 0 );
	fcvPyramidAllocate( src2Pyr, width, height, 4, nPyramidLevels, 0 );
	fcvPyramidAllocate( dx1Pyr, width, height, 4, nPyramidLevels, 1 );
	fcvPyramidAllocate( dy1Pyr, width, height, 4, nPyramidLevels, 1 );


	xy = new uint32_t[size*2];
	scores = new uint32_t[size];
	firstImage = true;
}

OpticalFlow::~OpticalFlow() {
	delete [] featureXY_in;
	delete [] featureXY_out;
	delete [] featureStatus_in;


}

int OpticalFlow::findFlow(char * image,int width, int  height, float * rect){

	Mat m(height,width,	CV_8UC1,(unsigned char *)image);

	resize(m,gray,Size(0,0),0.5,0.5);

	if(firstImage){
		gray.copyTo(prev);
		firstImage = false;
		return 0;
	}

	width = gray.cols;
	height = gray.rows;

	float left  = rect[0] * width;
	float top  = rect[1] * height;
	float right  = rect[2] * width;
	float bottom  = rect[3] * height;

	fcvCornerFast9u8((unsigned char *)prev.data, width, height, 0, 20, 7, xy, width*height, &corners);
	if(corners == 0){
		gray.copyTo(prev);
		__android_log_print(ANDROID_LOG_INFO,"OpticalFlow","ZERO FEATURES");
		return 0;
	}

	int n = 0;
	for( int i = 0; i < corners; i++ )
	{
		if(xy[i*2] <left || xy[i*2] > right || xy[i*2+1] < top || xy[i*2+1] > bottom){
			continue;
		}
		featureXY_in[n*2] = xy[i*2];
		featureXY_in[n*2+1] = xy[i*2+1];
		featureStatus_in[n++] = 0;
	}

	fcvPyramidCreateu8( prev.data, prev.cols, prev.rows, nPyramidLevels, src1Pyr );
	fcvPyramidCreateu8( gray.data, gray.cols, gray.rows, nPyramidLevels, src2Pyr );

	fcvPyramidSobelGradientCreatei8( src1Pyr, dx1Pyr, dy1Pyr, nPyramidLevels );

	fcvTrackLKOpticalFlowu8(
				prev.data, gray.data, gray.cols, gray.rows,
			src1Pyr, src2Pyr,
			dx1Pyr, dy1Pyr,
			featureXY_in,
			featureXY_out,
			featureStatus_in,
			/*featureXY_in.Shape().height*/n,//corners,
			//windowWidth, windowHeight,
			9,9,//gray.cols, gray.rows,
			/*maxIterations*/7, nPyramidLevels,
			/*maxResidue*/0.5, /*minDisplacement*/0.15, 0, 0 );

	Mat points1(corners,2,CV_32F,featureXY_in);
	Mat points2(corners,2,CV_32F,featureXY_out);
	Mat mask;

	if(n < 4){
		// we don't have enough points to compute homography
		__android_log_print(ANDROID_LOG_INFO,"OpticalFlow","NOT ENOUGH");
		gray.copyTo(prev);
		return -1;
	}

	Mat H = findHomography(points1, points2, mask, CV_RANSAC);
	//__android_log_print(ANDROID_LOG_INFO,"OpticalFlow"," 5__");
	double x = 0;
	double y = 0;
	int count = 0;
	for( int i = 0; i < n/*corners*/; i++ )
	{
		if( !(int)mask.at<uchar>(i,0) )
			   continue;
		x += featureXY_in[i*2]-featureXY_out[i*2];
		y += featureXY_in[i*2+1]-featureXY_out[i*2+1];
		count++;
		line(m,Point(featureXY_in[i*2],featureXY_in[i*2+1])*2,Point(featureXY_out[i*2],featureXY_out[i*2+1])*2,CV_RGB(255,255,255), 1, 8, 0);
	}
	x/=count;
	y/=count;
	rect[0]-=x/width;
	rect[1]-=y/height;
	rect[2]-=x/width;
	rect[3]-=y/height;
	gray.copyTo(prev);
	return 42;
}

