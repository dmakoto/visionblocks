/*==============================================================================
            Copyright (c) 2012-2013 QUALCOMM Austria Research Center GmbH.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary

 @file
    RefFreeFrameNative.h

 @brief
    A utility class for Native bindings used in the reference free frame.

 ==============================================================================*/
#ifndef _QCAR_REFFREEFRAME_NATIVE_H_
#define _QCAR_REFFREEFRAME_NATIVE_H_

#include <jni.h>

class RefFreeFrame;
class Texture;

class RefFreeFrameNative
{

public:
    static void
    init(RefFreeFrame* refFreeFrame);
    static bool
    getTexture(JNIEnv* env, jobject obj, Texture* &texture,
            const char * fileName);
    static void
    restartTracker( );
    static void
    targetCreatedCallback( );
    static void
    showErrorMessage();
};

#endif // _QCAR_REFFREEFRAME_NATIVE_H_
