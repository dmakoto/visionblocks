/*
 * vision_blocks.h
 *
 *  Created on: 12.2.2013
 *      Author: bubo
 */

#ifndef VISION_BLOCKS_H_
#define VISION_BLOCKS_H_

#include <jni.h>

extern "C" {
	JNIEXPORT jint JNICALL
		Java_edu_mit_cameraCulture_vblocks_data_GrayscaleNative_ConvertToGrayscale(
				JNIEnv* env,
				jobject thiz,
				jbyteArray byteArray,
				jintArray intArray,
				jint width,
				jint height);
}

#endif /* VISION_BLOCKS_H_ */
