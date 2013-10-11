/*==============================================================================
             Copyright (c) 2012-2013 QUALCOMM Austria Research Center GmbH.
             All Rights Reserved.
             Qualcomm Confidential and Proprietary

 @file
     RefFreeFrame.h

 @brief
     A utility class for textures used in the samples.

 ==============================================================================*/
#ifndef _QCAR_REFFREEFRAME_H_
#define _QCAR_REFFREEFRAME_H_

// Include files
#include <QCAR/ImageTargetBuilder.h>
#include <jni.h>
#include "Texture.h"
#include "RefFreeFrameGL.h"

// Forward declarations

/// A utility class for the RefFree frame.
class RefFreeFrame
{
public:
    /// Constructor
    RefFreeFrame();

    /// Destructor
    ~RefFreeFrame();

    /// Initializes the class (uses assets, need to be called from an Activity
    bool init(JNIEnv* env, jobject obj);

    /// Initializes (OpenGL-dependent and camera-dependent!!!) - Call when there's a valid GL context
    bool initGL(int screenWidth, int screenHeight);

    /// Deinitilizes the class
    void deInit();

    /// Updates the states and renders the hints, depending on the current status of the tracker
    void render();

    /// Resets the curStatus to IDLE
    void reset();

    // sets the state to CREATING
    void setCreating();

    /// Checks if a new trackable source is available
    bool hasNewTrackableSource();

    /// Retreives the available trackable source, returns NULL if the trackable
    // has already been retrieved
    QCAR::TrackableSource* getNewTrackableSource();

protected:
    
    /// Get the time in milliseconds
    unsigned int getTimeMS();

    /// The following methods rely on the internal state of this class
    /// when they are called.  Take care if extracting their functionality
    /// that you properly set up the environment.

    /// Updates the UI state in the render method
    void updateUIState(QCAR::ImageTargetBuilder * targetBuilder,
            QCAR::ImageTargetBuilder::FRAME_QUALITY frameQuality);

    /// Renders the scanning state view finder according to the result
    void renderScanningViewfinder(QCAR::ImageTargetBuilder::FRAME_QUALITY quality);

    /**
     * Enum to decribe the status of the UI showing the target 
     * creation state. 
     *  
     * IDLE - No target is currently being created 
     * SCANNING - Show the "Scanning UI", this uses frame quality info 
     *            from the SDK to give feedback to the user as to how
     *            good a target their current frame is.
     * CREATING - While in this state the SDK is creating the target 
     *            requested by the app layer.
     * SUCCESS - This state indicates that target creation was successful.
     */
    enum STATUS
    {
        STATUS_IDLE, STATUS_SCANNING, STATUS_CREATING, STATUS_SUCCESS
    };
    STATUS curStatus;

    /// Current color of the target finder.  This changes color 
    /// depending on frame quality.
    float colorFrame[4];

    /// Half of the screen size, used often in the rendering pipeline
    QCAR::Vec2F halfScreenSize;

    /// Keep track of the time between frames for color transitions
    unsigned int lastFrameTime;
    unsigned int lastSuccessTime;

    /// All rendering methods are contained in this class for easy 
    /// extraction/abstraction
    RefFreeFrameGL * frameGL;

    /// The latest trackable source to be extracted from the Target Builder
    QCAR::TrackableSource* trackableSource;

private:

    /// Hidden copy constructor
    RefFreeFrame(const RefFreeFrame &);

    /// Hidden assignment operator
    RefFreeFrame& operator=(const RefFreeFrame &);

};

#endif //_QCAR_REFFREEFRAME_H_
