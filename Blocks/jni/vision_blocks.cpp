#include <iostream>
#include <string>

#include <jni.h>
//#include <cpu-features.h>
#include <android/log.h>

#include "vision_blocks.h"
#include "opencv2/opencv.hpp"

using namespace cv;

JNIEXPORT jint JNICALL
	Java_edu_mit_cameraCulture_vblocks_data_GrayscaleNative_ConvertToGrayscale(
			JNIEnv* env,
			jobject thiz,
			jbyteArray byteArray,
			jintArray intArray,
			jint width,
			jint height){

	jboolean intArray_isCopy;
	jint * intArray_bytes = env->GetIntArrayElements(intArray, &intArray_isCopy);
	int * intImage = reinterpret_cast<int *>(intArray_bytes);

	jboolean byteArray_isCopy;
	jbyte * byteArray_bytes = env->GetByteArrayElements(byteArray, &byteArray_isCopy);
	char * byteImage = reinterpret_cast<char *>(byteArray_bytes);

	Mat m1(height,width,	CV_8UC4,(unsigned char *)byteImage);
	Mat m2(height,width,	CV_8UC4,(unsigned char *)intImage);
	cvtColor(m1,m1,CV_RGBA2GRAY);
	cvtColor(m1,m2,CV_GRAY2RGBA);
//	for(int i=0; i<width*height;i++){
//		intImage[i] = 0xffffffff;
	//}
	env->ReleaseByteArrayElements(byteArray,byteArray_bytes,0);
	env->ReleaseIntArrayElements(intArray,intArray_bytes,0);
	return 42;
}
