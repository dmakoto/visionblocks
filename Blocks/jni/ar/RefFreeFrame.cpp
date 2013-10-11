/*==============================================================================
             Copyright (c) 2012-2013 QUALCOMM Austria Research Center GmbH.
             All Rights Reserved.
             Qualcomm Confidential and Proprietary

 @file
     RefFreeFrame.cpp

 @brief
     Implementation of class RefFreeFrame.

 ==============================================================================*/

// ** Include files
#include <time.h>
#include <QCAR/QCAR.h>
#include <QCAR/TrackerManager.h>
#include <QCAR/ImageTracker.h>
#include <QCAR/ImageTargetBuilder.h>
#include <QCAR/VideoBackgroundConfig.h>
#include <QCAR/Renderer.h>
#include "RefFreeFrame.h"
#include "RefFreeFrameNative.h"
#include "SampleUtils.h"

// ** Some helper functions

/// Function used to transition in the range [0, 1]
void
transition(float &v0, float inc, float a = 0.0f, float b = 1.0f)
{
    float vOut = v0 + inc;
    v0 = (vOut < a ? a : (vOut > b ? b : vOut));
}

// ** Methods

RefFreeFrame::RefFreeFrame( ) :
    curStatus(STATUS_IDLE), lastSuccessTime(0), trackableSource(NULL)
{
    colorFrame[0] = 1.0f;
    colorFrame[1] = 0.0f;
    colorFrame[2] = 0.0f;
    colorFrame[3] = 0.75f;

    frameGL = new RefFreeFrameGL();
}

RefFreeFrame::~RefFreeFrame( )
{
    delete frameGL;
}

bool
RefFreeFrame::init(JNIEnv* env, jobject obj)
{
    // initialize the native portion of the frame
    RefFreeFrameNative::init(this);

    // load the frame texture
    frameGL->getTextures(env, obj);

    trackableSource = NULL;
}

void
RefFreeFrame::deInit( )
{
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker =
            static_cast<QCAR::ImageTracker*> (trackerManager.getTracker(
                    QCAR::Tracker::IMAGE_TRACKER));
    if (imageTracker != 0)
    {
        QCAR::ImageTargetBuilder* targetBuilder =
                imageTracker->getImageTargetBuilder();
        if (targetBuilder && (targetBuilder->getFrameQuality()
                != QCAR::ImageTargetBuilder::FRAME_QUALITY_NONE))
        {
            targetBuilder->stopScan();
        }
    }
}

bool
RefFreeFrame::initGL(int screenWidth, int screenHeight)
{
    frameGL->init(screenWidth, screenHeight);

    QCAR::Renderer &renderer = QCAR::Renderer::getInstance();
    const QCAR::VideoBackgroundConfig &vc = renderer.getVideoBackgroundConfig();
    halfScreenSize.data[0] = vc.mSize.data[0] * 0.5f;
    halfScreenSize.data[1] = vc.mSize.data[1] * 0.5f;

    // sets last frame timer
    lastFrameTime = getTimeMS();

    reset();
}

void
RefFreeFrame::reset( )
{
    curStatus = STATUS_IDLE;

}

void
RefFreeFrame::setCreating( )
{
    curStatus = STATUS_CREATING;
}

void
RefFreeFrame::updateUIState(QCAR::ImageTargetBuilder * targetBuilder,
        QCAR::ImageTargetBuilder::FRAME_QUALITY frameQuality)
{
    // ** Elapsed time
    unsigned int elapsedTimeMS = getTimeMS() - lastFrameTime;
    lastFrameTime += elapsedTimeMS;

    // This is a time-dependent value used for transitions in the range [0, 1] 
    // over the period of half of a second.
    float transitionHalfSecond = elapsedTimeMS * 0.002f;

    STATUS newStatus(curStatus);

    switch (curStatus)
    {
    case STATUS_IDLE:
        if (frameQuality != QCAR::ImageTargetBuilder::FRAME_QUALITY_NONE)
            newStatus = STATUS_SCANNING;

        break;

    case STATUS_SCANNING:
        switch (frameQuality)
        {
        // bad target quality, render the frame white until a match is made, then go to green
        case QCAR::ImageTargetBuilder::FRAME_QUALITY_LOW:
            colorFrame[0] = 1.0f;
            colorFrame[1] = 1.0f;
            colorFrame[2] = 1.0f;

            break;

            // good target, switch to green over half a second
        case QCAR::ImageTargetBuilder::FRAME_QUALITY_HIGH:
        case QCAR::ImageTargetBuilder::FRAME_QUALITY_MEDIUM:
            transition(colorFrame[0], -transitionHalfSecond);
            transition(colorFrame[1], transitionHalfSecond);
            transition(colorFrame[2], -transitionHalfSecond);

            break;
        }
        break;

    case STATUS_CREATING:
    {
        // check for new result
        // if found, set to success, success time and:
        QCAR::TrackableSource* newTrackableSource =
                targetBuilder->getTrackableSource();
        if (newTrackableSource != NULL)
        {
            newStatus = STATUS_SUCCESS;
            lastSuccessTime = lastFrameTime;
            trackableSource = newTrackableSource;
            targetBuilder->stopScan();

            RefFreeFrameNative::targetCreatedCallback();
        }
    }
    default:
        break;
    }

    curStatus = newStatus;
}

void
RefFreeFrame::render( )
{
    // ** Get the image tracker
    QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
    QCAR::ImageTracker* imageTracker =
            static_cast<QCAR::ImageTracker*> (trackerManager.getTracker(
                    QCAR::Tracker::IMAGE_TRACKER));

    // Get the frame quality from the target builder
    QCAR::ImageTargetBuilder* targetBuilder =
            imageTracker->getImageTargetBuilder();
    QCAR::ImageTargetBuilder::FRAME_QUALITY frameQuality =
            targetBuilder->getFrameQuality();

    // Update the UI internal state variables
    updateUIState(targetBuilder, frameQuality);

    if (curStatus == STATUS_SUCCESS)
    {
        curStatus = STATUS_IDLE;

        LOG("Built target, reactivating dataset with new target");
        RefFreeFrameNative::restartTracker();
    }

    // ** Renders the hints
    switch (curStatus)
    {
    case STATUS_SCANNING:
        renderScanningViewfinder(frameQuality);
        break;

    }

    SampleUtils::checkGlError("RefFreeFrame render");
}

void
RefFreeFrame::renderScanningViewfinder(
        QCAR::ImageTargetBuilder::FRAME_QUALITY quality)
{
    frameGL->setModelViewScale(2.0f);
    frameGL->setColor(colorFrame);
    frameGL->renderViewfinder();

}

unsigned int
RefFreeFrame::getTimeMS( )
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

bool
RefFreeFrame::hasNewTrackableSource( )
{
    return (trackableSource != NULL);
}

QCAR::TrackableSource*
RefFreeFrame::getNewTrackableSource( )
{
    QCAR::TrackableSource * result = trackableSource;
    trackableSource = NULL;
    return result;
}

