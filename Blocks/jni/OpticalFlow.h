/*
 * OpticalFlow.h
 *
 *  Created on: 14.3.2013
 *      Author: bubo
 */

#ifndef OPTICALFLOW_H_
#define OPTICALFLOW_H_

#include <fastcv.h>
#include <jni.h>
#include "opencv2/opencv.hpp"

using namespace cv;


extern "C" {
	JNIEXPORT jint JNICALL
		Java_edu_mit_cameraCulture_vblocks_predefined_OpticalFlow_processImage(
				JNIEnv* env,
				jobject thiz,
				jbyteArray byteArray,
				jint width,
				jint height,
				jfloatArray rect);

	JNIEXPORT jint JNICALL
			Java_edu_mit_cameraCulture_vblocks_predefined_OpticalFlow_clean(
					JNIEnv* env,
					jobject thiz);
}



class OpticalFlow {
public:
	OpticalFlow(int width, int height);
	virtual ~OpticalFlow();
	int findFlow(char * image,int width, int  height, float * rect);

private:
	float * featureXY_in;
	float * featureXY_out;
	int32_t * featureStatus_in;

	uint32_t corners;
	uint32_t * xy;
	uint32_t * scores;
	fcvPyramidLevel * src1Pyr;
	fcvPyramidLevel * src2Pyr;
	fcvPyramidLevel * dx1Pyr;
	fcvPyramidLevel * dy1Pyr;
	uint32_t nPyramidLevels;

	Mat gray;
	Mat prev;

	bool firstImage;
};

#endif /* OPTICALFLOW_H_ */
