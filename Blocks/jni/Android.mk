LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := QCAR-prebuilt
LOCAL_SRC_FILES = ../libs/libQCAR.so
LOCAL_EXPORT_C_INCLUDES := ar/include
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)

#OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
#OPENCV_LIB_TYPE:=STATIC

include $(OPENCVROOT)/native/jni/OpenCV.mk  #C:/Stuff/vision/opencv/android/build/OpenCV.mk  

OPENGLES_LIB  := -lGLESv2
OPENGLES_DEF  := -DUSE_OPENGL_ES_2_0

LOCAL_C_INCLUDES :=  $(OPENCVROOT)/native/jni/include $(LOCAL_PATH) $(LOCAL_PATH)/include $(LOCAL_PATH)/ar/include 

LOCAL_MODULE    := vision_blocks
LOCAL_SRC_FILES := vision_blocks.cpp OpticalFlow.cpp \
		ar/Augmenter.cpp \
		ar/RefFreeFrame.cpp \
		ar/RefFreeFrameGL.cpp \
		ar/RefFreeFrameNative.cpp \
		ar/SampleUtils.cpp \
		ar/Texture.cpp
					

#ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
#    LOCAL_CFLAGS := -g -DHAVE_NEON=1
#    LOCAL_SRC_FILES += pano_stitching-intrinsics.cpp.neon
#endif



LOCAL_LDLIBS := -llog $(OPENGLES_LIB) #-lfastcv
LOCAL_LDLIBS += libs/libfastcv.a

LOCAL_SHARED_LIBRARIES += QCAR-prebuilt

LOCAL_CPPFLAGS += -g
LOCAL_CFLAGS := -Wno-write-strings -Wno-psabi $(OPENGLES_DEF)

include $(BUILD_SHARED_LIBRARY)



#$(call import-module,cpufeatures)