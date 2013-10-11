/*==============================================================================
            Copyright (c) 2012-2013 QUALCOMM Austria Research Center GmbH.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary

 @file
    RefFreeFrameNative.cpp

 @brief
    Implementation of class RefFreeFrameNative.

 ==============================================================================*/

#include <QCAR/TrackerManager.h>
#include <QCAR/ImageTracker.h>
#include <QCAR/ImageTargetBuilder.h>
#include "RefFreeFrameNative.h"
#include "RefFreeFrame.h"
#include "Texture.h"
#include "SampleUtils.h"

// **JNI book keeping to call back up to the Java layer to give user feedback
// with native UI
static JavaVM* g_javaVM = NULL;
static jobject g_Augmenter = NULL;
jmethodID targetCreatedID;
jmethodID showErrorDialogID;
int targetBuilderCounter = 1;
RefFreeFrame* g_refFreeFrame = 0;

#ifdef __cplusplus
extern "C"
{
#endif

JNIEnv*
getJNIEnv( )
{
    if (g_javaVM == NULL)
    {
        return NULL;
    }

    JNIEnv* env;
    if (g_javaVM->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK)
    {
        return NULL;
    }
    else
    {
        return env;
    }
}

JNIEXPORT void JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_initRefFreeNative(JNIEnv* env, jobject self)
{
    if(self == NULL)
    {
        LOG("ERROR : Must provide a reference to Augmenter");
        return;
    }

    g_Augmenter = env->NewGlobalRef(self);
    env->GetJavaVM(&g_javaVM);
    jclass clsAugmenter = env->GetObjectClass(g_Augmenter);
    if(!clsAugmenter)
    {
        LOG("ERROR : Unable to find the class type of the provided object");
        return;
    }

    targetCreatedID = env->GetMethodID(clsAugmenter,"targetCreated", "()V");

    showErrorDialogID = env->GetMethodID(clsAugmenter,"showErrorDialogInUIThread", "()V");
}

JNIEXPORT jboolean JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_startAugmenter(JNIEnv*, jobject)
{
    LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_startAugmenter");

    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));
    if(imageTracker != 0)
    {
        QCAR::ImageTargetBuilder* targetBuilder = imageTracker->getImageTargetBuilder();

        if (targetBuilder)
        {
            // if needed, stop the target builder
            if (targetBuilder->getFrameQuality() != QCAR::ImageTargetBuilder::FRAME_QUALITY_NONE)
            targetBuilder->stopScan();

            imageTracker->stop();

            targetBuilder->startScan();

        }
    }
    else
    return JNI_FALSE;

    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_stopAugmenter(JNIEnv*, jobject)
{
    LOG("Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_stopAugmenter");

    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));

    if(imageTracker)
    {
        QCAR::ImageTargetBuilder* targetBuilder = imageTracker->getImageTargetBuilder();
        if (targetBuilder)
        {
            if (targetBuilder->getFrameQuality() != QCAR::ImageTargetBuilder::FRAME_QUALITY_NONE)
            {
                targetBuilder->stopScan();
                targetBuilder = NULL;

                if(g_refFreeFrame != NULL)
                {
                    g_refFreeFrame->reset();
                }

                QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
                QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));

                imageTracker->start();
            }
        }
        else
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_isAugmenterRunning(JNIEnv*, jobject)
{
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));

    if(imageTracker)
    {
        QCAR::ImageTargetBuilder* targetBuilder = imageTracker->getImageTargetBuilder();
        if(targetBuilder)
        {
            return (targetBuilder->getFrameQuality() != QCAR::ImageTargetBuilder::FRAME_QUALITY_NONE) ? JNI_TRUE : JNI_FALSE;
        }
    }

    return JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_edu_mit_cameraCulture_vblocks_predefined_augmenter_Augmenter_startBuild(JNIEnv*, jobject)
{
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();

    QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));
    //QCAR::DataSet * d = imageTracker->createDataSet();

    if(imageTracker)
    {
        QCAR::ImageTargetBuilder* targetBuilder = imageTracker->getImageTargetBuilder();
        if(targetBuilder)
        {
            // Before creating the target checks current frame quality. If the frame
            // quality is Low it shows an error message to the user
            if(targetBuilder->getFrameQuality() == QCAR::ImageTargetBuilder::FRAME_QUALITY_LOW)
            {
                RefFreeFrameNative::showErrorMessage( );
            }

            char name[128];
            do
            {
                snprintf(name, sizeof(name), "AugmenterTarget-%d", targetBuilderCounter++);
                LOG("TRYING %s", name);
            }
            while (!targetBuilder->build(name, 320.0));

            g_refFreeFrame->setCreating();
        }
    }
}

#ifdef __cplusplus
}
#endif

void
RefFreeFrameNative::init(RefFreeFrame* refFreeFrame)
{
    g_refFreeFrame = refFreeFrame;
}

bool
RefFreeFrameNative::getTexture(JNIEnv* env, jobject obj, Texture* &texture,
        const char * fileName)
{
    jclass activityClass = env->GetObjectClass(obj);
    jmethodID
            createTextureMethodID =
                    env->GetMethodID(activityClass, "createTexture",
                            "(Ljava/lang/String;)Ledu/mit/cameraCulture/vblocks/predefined/augmenter/Texture;");

    if (createTextureMethodID == 0)
        return false;

    jobject textureObject = env->CallObjectMethod(obj, createTextureMethodID,
            env->NewStringUTF(fileName));
    if (textureObject != NULL)
        texture = Texture::create(env, textureObject);
    else
        return false;

    return (texture != NULL);
}

void
RefFreeFrameNative::restartTracker( )
{
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker =
            static_cast<QCAR::ImageTracker*> (trackerManager.getTracker(
                    QCAR::Tracker::IMAGE_TRACKER));

    imageTracker->start();
}

void
RefFreeFrameNative::targetCreatedCallback( )
{
    getJNIEnv()->CallVoidMethod(g_Augmenter, targetCreatedID);
}

void
RefFreeFrameNative::showErrorMessage( )
{
    getJNIEnv()->CallVoidMethod(g_Augmenter, showErrorDialogID);
}

