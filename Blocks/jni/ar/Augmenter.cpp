/*==============================================================================
            Copyright (c) 2012-2013 QUALCOMM Austria Research Center GmbH.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary

 @file
    Augmenter.cpp

 @brief
    Sample for Augmenter

 ==============================================================================*/

#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <math.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <QCAR/QCAR.h>
#include <QCAR/CameraDevice.h>
#include <QCAR/Renderer.h>
#include <QCAR/VideoBackgroundConfig.h>
#include <QCAR/Trackable.h>
#include <QCAR/TrackableResult.h>
#include <QCAR/Tool.h>
#include <QCAR/Tracker.h>
#include <QCAR/TrackerManager.h>
#include <QCAR/ImageTracker.h>
#include <QCAR/CameraCalibration.h>
#include <QCAR/UpdateCallback.h>
#include <QCAR/DataSet.h>
#include <QCAR/ImageTargetBuilder.h>
#include "SampleUtils.h"
#include "Texture.h"
#include "CubeShaders.h"
#include "Teapot.h"
#include "RefFreeFrame.h"

#ifdef __cplusplus
extern "C"
{
#endif

// Reference Free Frame data:
RefFreeFrame refFreeFrame;

// Textures:
int textureCount = 0;
Texture** textures = 0;

// OpenGL ES 2.0 specific:
unsigned int shaderProgramID = 0;
GLint vertexHandle = 0;
GLint normalHandle = 0;
GLint textureCoordHandle = 0;
GLint mvpMatrixHandle = 0;
GLint texSampler2DHandle        = 0;

// Screen dimensions:
unsigned int screenWidth = 0;
unsigned int screenHeight = 0;

// Indicates whether screen is in portrait (true) or landscape (false) mode
bool isActivityInPortraitMode = false;

// The projection matrix used for rendering virtual objects:
QCAR::Matrix44F projectionMatrix;

// Constants:
static const float kObjectScale = 3.f;

QCAR::DataSet* dataSetUserDef = 0;

// Object to receive update callbacks from QCAR SDK
class Augmenter_UpdateCallback : public QCAR::UpdateCallback
{
    virtual void
    QCAR_onUpdate(QCAR::State& /*state*/)
    {
        QCAR::TrackerManager& trackerManager =
                QCAR::TrackerManager::getInstance();
        QCAR::ImageTracker* imageTracker =
                static_cast<QCAR::ImageTracker*> (trackerManager.getTracker(
                        QCAR::Tracker::IMAGE_TRACKER));

        if (refFreeFrame.hasNewTrackableSource())
        {
            LOG("Attempting to transfer the trackable source to the dataset");

            // Deactivate current dataset
            imageTracker->deactivateDataSet(imageTracker->getActiveDataSet());

            // Clear the oldest target if the dataset is full or the dataset 
            // already contains five user-defined targets.
            if (dataSetUserDef->hasReachedTrackableLimit()
                    || dataSetUserDef->getNumTrackables() >= 5)
                dataSetUserDef->destroy(dataSetUserDef->getTrackable(0));

            // Add new trackable source
            dataSetUserDef->createTrackable(
                    refFreeFrame.getNewTrackableSource());

            // Reactivate current dataset
            imageTracker->activateDataSet(dataSetUserDef);
        }
    }
};

Augmenter_UpdateCallback updateCallback;

JNIEXPORT void JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_setActivityPortraitMode(JNIEnv *, jobject, jboolean isPortrait)
{
    isActivityInPortraitMode = isPortrait;
}

JNIEXPORT int JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_initTracker(JNIEnv *, jobject)
{
    LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_initTracker");

    // Initialize the image tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::Tracker* tracker = trackerManager.initTracker(QCAR::Tracker::IMAGE_TRACKER);
    if (tracker == NULL)
    {
        LOG("Failed to initialize ImageTracker.");
        return 0;
    }

    LOG("Successfully initialized ImageTracker.");
    return 1;
}

JNIEXPORT void JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_deinitTracker(JNIEnv *, jobject)
{
    LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_deinitTracker");

    refFreeFrame.deInit();

    // Deinit the image tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    trackerManager.deinitTracker(QCAR::Tracker::IMAGE_TRACKER);
}

JNIEXPORT int JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_loadTrackerData(JNIEnv *, jobject)
{
    LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_loadTrackerData");

    // Get the image tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(
            trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));
    if (imageTracker == NULL)
    {
        LOG("Failed to load tracking data set because the ImageTracker has not"
                " been initialized.");
        return 0;
    }

    // Create the data set:
    dataSetUserDef = imageTracker->createDataSet();
    if (dataSetUserDef == 0)
    {
        LOG("Failed to create a new tracking data.");
        return 0;
    }

    if (!imageTracker->activateDataSet(dataSetUserDef))
    {
        LOG("Failed to activate data set.");
        return 0;
    }

    LOG("Successfully loaded and activated data set.");
    return 1;
}

JNIEXPORT int JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_destroyTrackerData(JNIEnv *, jobject)
{
    LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_destroyTrackerData");

    // Get the image tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(
            trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));
    if (imageTracker == NULL)
    {
        LOG("Failed to destroy the tracking data set because the ImageTracker has not"
                " been initialized.");
        return 0;
    }

    if (dataSetUserDef != 0)
    {
        if (imageTracker->getActiveDataSet() && !imageTracker->deactivateDataSet(dataSetUserDef))
        {
            LOG("Failed to destroy the tracking data set because the data set "
                    "could not be deactivated.");
            return 0;
        }

        if (!imageTracker->destroyDataSet(dataSetUserDef))
        {
            LOG("Failed to destroy the tracking data set.");
            return 0;
        }

        LOG("Successfully destroyed the data set.");
        dataSetUserDef = 0;
        return 1;
    }

    LOG("No tracker data set to destroy.");
    return 0;
}

JNIEXPORT void JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_onQCARInitializedNative(JNIEnv *, jobject)
{
    QCAR::registerCallback(&updateCallback);

    // Comment in to enable tracking of up to 2 targets simultaneously
    // QCAR::setHint(QCAR::HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 2);
}

JNIEXPORT jint JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_AugmenterRenderer_renderFrame(JNIEnv * env, jobject obj)
{
//		// Clear color and depth buffer
//	    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//	    // Get the state from QCAR and mark the beginning of a rendering section
//	    QCAR::State state = QCAR::Renderer::getInstance().begin();
//	    // Explicitly render the Video Background
//	    QCAR::Renderer::getInstance().drawVideoBackground();
//	    // Did we find any trackables this frame?
//	    for(int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
//	    {
//	        // Get the trackable:
//	        const QCAR::TrackableResult* result = state.getTrackableResult(tIdx);
//	        const QCAR::Trackable& trackable = result->getTrackable();
//	        QCAR::Matrix44F modelViewMatrix = QCAR::Tool::convertPose2GLMatrix(result->getPose());
//	    }
//	    QCAR::Renderer::getInstance().end();
    //LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_GLRenderer_renderFrame");
	jint ret = 0;
	jclass activityClass = env->GetObjectClass(obj); //We get the class of out activity
	jmethodID updateMatrixMethod = env->GetMethodID(activityClass, "updateModelviewMatrix", "([F)V");

    // Clear color and depth buffer
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Get the state from QCAR and mark the beginning of a rendering section
    QCAR::State state = QCAR::Renderer::getInstance().begin();

    // Explicitly render the Video Background
    QCAR::Renderer::getInstance().drawVideoBackground();

    //glEnable(GL_DEPTH_TEST);
    //glEnable(GL_CULL_FACE);

    // Render the RefFree UI elements depending on the current state
    refFreeFrame.render();

    jfloatArray modelviewArray = env->NewFloatArray(16);


    // Did we find any trackables this frame?
    for(int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
    {
    	ret = state.getNumTrackableResults();
        // Get the trackable:
        const QCAR::TrackableResult* trackableResult = state.getTrackableResult(tIdx);
        const QCAR::Trackable& trackable = trackableResult->getTrackable();
        QCAR::Matrix44F modelViewMatrix =
        QCAR::Tool::convertPose2GLMatrix(trackableResult->getPose());

        SampleUtils::rotatePoseMatrix(180.0f, 1.0f, 0, 0, &modelViewMatrix.data[0]);
        // Passes the model view matrix to java
        env->SetFloatArrayRegion(modelviewArray, 0, 16, modelViewMatrix.data);
        env->CallVoidMethod(obj, updateMatrixMethod , modelviewArray);

        // Choose the texture based on the target name:
        int textureIndex = 1;

        const Texture* const thisTexture = textures[textureIndex];

        QCAR::Matrix44F modelViewProjection;

//        SampleUtils::translatePoseMatrix(0.0f, 0.0f, kObjectScale,
//                &modelViewMatrix.data[0]);
//        SampleUtils::scalePoseMatrix(kObjectScale, kObjectScale, kObjectScale,
//                &modelViewMatrix.data[0]);
//        SampleUtils::multiplyMatrix(&projectionMatrix.data[0],
//                &modelViewMatrix.data[0] ,
//                &modelViewProjection.data[0]);

        glUseProgram(shaderProgramID);

//        glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0,
//                (const GLvoid*) &teapotVertices[0]);
//        glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0,
//                (const GLvoid*) &teapotNormals[0]);
//        glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0,
//                (const GLvoid*) &teapotTexCoords[0]);

//        glEnableVertexAttribArray(vertexHandle);
//        glEnableVertexAttribArray(normalHandle);
//        glEnableVertexAttribArray(textureCoordHandle);

//        glActiveTexture(GL_TEXTURE0);
//        glBindTexture(GL_TEXTURE_2D, thisTexture->mTextureID);
//        glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE,
//                (GLfloat*)&modelViewProjection.data[0] );
//        glUniform1i(texSampler2DHandle, 0 /*GL_TEXTURE0*/);
//        glDrawElements(GL_TRIANGLES, NUM_TEAPOT_OBJECT_INDEX, GL_UNSIGNED_SHORT,
//                (const GLvoid*) &teapotIndices[0]);

        SampleUtils::checkGlError("Augmenter renderFrame");
    }

    const QCAR::CameraCalibration& cameraCalibration = QCAR::CameraDevice::getInstance().getCameraCalibration();
    QCAR::Vec2F size = cameraCalibration.getSize();
    QCAR::Vec2F focalLength = cameraCalibration.getFocalLength();
    float fovyRadians = 2 * atan(0.5f * size.data[1] / focalLength.data[1]);
    float fovRadians = 2 * atan(0.5f * size.data[0] / focalLength.data[0]);

    jmethodID fovMethod = env->GetMethodID(activityClass, "setFov", "(F)V");
    jmethodID fovyMethod = env->GetMethodID(activityClass, "setFovy", "(F)V");

    env->CallVoidMethod(obj, fovMethod, fovRadians);
    env->CallVoidMethod(obj, fovyMethod, fovyRadians);

    env->DeleteLocalRef(modelviewArray);

//    glDisable(GL_DEPTH_TEST);

    glDisableVertexAttribArray(vertexHandle);
    glDisableVertexAttribArray(normalHandle);
    glDisableVertexAttribArray(textureCoordHandle);

    QCAR::Renderer::getInstance().end();
    return ret;
}

void
configureVideoBackground( )
{
    // Get the default video mode:
    QCAR::CameraDevice& cameraDevice = QCAR::CameraDevice::getInstance();
    QCAR::VideoMode videoMode = cameraDevice.getVideoMode(
            QCAR::CameraDevice::MODE_DEFAULT);

    // Configure the video background
    QCAR::VideoBackgroundConfig config;
    config.mEnabled = true;
    config.mSynchronous = true;
    config.mPosition.data[0] = 0.0f;
    config.mPosition.data[1] = 0.0f;

    if (isActivityInPortraitMode)
    {
        config.mSize.data[0] = videoMode.mHeight * (screenHeight / (float)videoMode.mWidth);
        config.mSize.data[1] = screenHeight;

        if (config.mSize.data[0] < screenWidth)
        {
            LOG("Correcting rendering background size to handle mismatch between screen and video aspect ratios.");
            config.mSize.data[0] = screenWidth;
            config.mSize.data[1] = screenWidth * (videoMode.mWidth  / (float)videoMode.mHeight);
        }
    }
    else
    {
        config.mSize.data[0] = screenWidth;
        config.mSize.data[1] = videoMode.mHeight * (screenWidth
                / (float)videoMode.mWidth);

        if (config.mSize.data[1] < screenHeight)
        {
            LOG("Correcting rendering background size to handle mismatch between screen and video aspect ratios.");
            config.mSize.data[0] = screenHeight * (videoMode.mWidth / (float)videoMode.mHeight);
            config.mSize.data[1] = screenHeight;
        }
    }

    LOG("Configure Video Background : Video (%d,%d), Screen (%d,%d), mSize (%d,%d)",
         videoMode.mWidth, videoMode.mHeight, screenWidth, screenHeight,
         config.mSize.data[0], config.mSize.data[1]);

    // Set the config:
    QCAR::Renderer::getInstance().setVideoBackgroundConfig(config);

}

JNIEXPORT void JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_initApplicationNative(
        JNIEnv* env, jobject obj, jint width, jint height)
{
    LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_initApplicationNative");

    // Store screen dimensions
    screenWidth = width;
    screenHeight = height;

    // Handle to the activity class:
    jclass activityClass = env->GetObjectClass(obj);

    jmethodID getTextureCountMethodID = env->GetMethodID(activityClass,
            "getTextureCount", "()I");
    if (getTextureCountMethodID == 0)
    {
        LOG("Function getTextureCount() not found.");
        return;
    }

    textureCount = env->CallIntMethod(obj, getTextureCountMethodID);
    if (!textureCount)
    {
        LOG("getTextureCount() returned zero.");
        return;
    }

    textures = new Texture*[textureCount];

    jmethodID getTextureMethodID = env->GetMethodID(activityClass,
            "getTexture", "(I)Ledu/mit/cameraCulture/vblocks/predefined/augmenter/Texture;");

    if (getTextureMethodID == 0)
    {
        LOG("Function getTexture() not found.");
        return;
    }

    // Register the textures
    for (int i = 0; i < textureCount; ++i)
    {

        jobject textureObject = env->CallObjectMethod(obj, getTextureMethodID, i);
        if (textureObject == NULL)
        {
            LOG("GetTexture() returned zero pointer");
            return;
        }

        textures[i] = Texture::create(env, textureObject);
    }

    refFreeFrame.init(env, obj);

    LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_initApplicationNative finished");
}

JNIEXPORT void JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_deinitApplicationNative(
        JNIEnv* env, jobject obj)
{
    LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_deinitApplicationNative");

    // Release texture resources
    if (textures != 0)
    {
        for (int i = 0; i < textureCount; ++i)
        {
            delete textures[i];
            textures[i] = NULL;
        }

        delete[]textures;
        textures = NULL;

        textureCount = 0;
    }
}

JNIEXPORT void JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_startCamera(JNIEnv *,
        jobject)
{
    LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_startCamera");

    // Initialize the camera:
    if (!QCAR::CameraDevice::getInstance().init())
    return;

    // Configure the video background
    configureVideoBackground();

    // Select the default mode:
    if (!QCAR::CameraDevice::getInstance().selectVideoMode(
                    QCAR::CameraDevice::MODE_DEFAULT))
    return;

    // Start the camera:
    if (!QCAR::CameraDevice::getInstance().start())
    return;

    // Uncomment to enable infinity focus mode, or any other supported focus mode
    // See CameraDevice.h for supported focus modes
    //if(QCAR::CameraDevice::getInstance().setFocusMode(QCAR::CameraDevice::FOCUS_MODE_INFINITY))
    //	LOG("IMAGE TARGETS : enabled infinity focus");

    // Start the tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::Tracker* imageTracker = trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER);
    if(imageTracker != 0)
    imageTracker->start();
}

JNIEXPORT void JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_stopCamera(JNIEnv *,
        jobject)
{
    LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_stopCamera");

    // Stop the tracker:
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::Tracker* imageTracker = trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER);
    if(imageTracker != 0)
    imageTracker->stop();

    QCAR::CameraDevice::getInstance().stop();
    QCAR::CameraDevice::getInstance().deinit();
}

JNIEXPORT void JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_setProjectionMatrix(JNIEnv *, jobject)
{
    LOG("Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_setProjectionMatrix");

    // Cache the projection matrix:
    const QCAR::CameraCalibration& cameraCalibration =
    QCAR::CameraDevice::getInstance().getCameraCalibration();
    projectionMatrix = QCAR::Tool::getProjectionGL(cameraCalibration, 2.0f, 2500.0f);
}

// ----------------------------------------------------------------------------
// Activates Camera Flash
// ----------------------------------------------------------------------------
JNIEXPORT jboolean JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_activateFlash(JNIEnv*, jobject, jboolean flash)
{
    return QCAR::CameraDevice::getInstance().setFlashTorchMode((flash==JNI_TRUE)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_autofocus(JNIEnv*, jobject)
{
    return QCAR::CameraDevice::getInstance().setFocusMode(QCAR::CameraDevice::FOCUS_MODE_TRIGGERAUTO)?JNI_TRUE:JNI_FALSE;
}

// ----------------------------------------------------------------------------
// Sets the Camera Focus Mode
// ----------------------------------------------------------------------------
JNIEXPORT jboolean JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_setFocusMode(JNIEnv*, jobject, jint mode)
{
    int qcarFocusMode;

    switch ((int)mode)
    {
        case 0:
        qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_NORMAL;
        break;

        case 1:
        qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_CONTINUOUSAUTO;
        break;

        case 2:
        qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_INFINITY;
        break;

        case 3:
        qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_MACRO;
        break;

        default:
        return JNI_FALSE;
    }

    return QCAR::CameraDevice::getInstance().setFocusMode(qcarFocusMode) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_AugmenterRenderer_initRendering(
        JNIEnv* env, jobject obj)
{
    LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_AugmenterRenderer_initRendering");

    // Define clear color
    glClearColor(0.0f, 0.0f, 0.0f, QCAR::requiresAlpha() ? 0.0f : 1.0f);

    // Now generate the OpenGL texture objects and add settings
    for (int i = 0; i < textureCount; ++i)
    {
        glGenTextures(1, &(textures[i]->mTextureID));
        glBindTexture(GL_TEXTURE_2D, textures[i]->mTextureID);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textures[i]->mWidth,
                textures[i]->mHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                (GLvoid*) textures[i]->mData);
    }

    shaderProgramID = SampleUtils::createProgramFromBuffer(cubeMeshVertexShader,
            cubeFragmentShader);

    vertexHandle = glGetAttribLocation(shaderProgramID,
            "vertexPosition");
    normalHandle = glGetAttribLocation(shaderProgramID,
            "vertexNormal");
    textureCoordHandle = glGetAttribLocation(shaderProgramID,
            "vertexTexCoord");
    mvpMatrixHandle = glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");

    texSampler2DHandle  = glGetUniformLocation(shaderProgramID,
                                                    "texSampler2D");
}

JNIEXPORT void JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_AugmenterRenderer_updateRendering(
        JNIEnv* env, jobject obj, jint width, jint height)
{
    LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_AugmenterRenderer_updateRendering");

    // Update screen dimensions
    screenWidth = width;
    screenHeight = height;

    // Reconfigure the video background
    configureVideoBackground();

    refFreeFrame.initGL(screenWidth, screenHeight);
}

#ifdef __cplusplus
}
#endif
