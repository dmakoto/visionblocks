/*==============================================================================
            Copyright (c) 2012-2013 QUALCOMM Austria Research Center GmbH.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary

 @file
    RefFreeFrameGL.h

 @brief
    A utility class for textures used in the samples.

 ==============================================================================*/
#ifndef _QCAR_REFFREEFRAME_GL_H_
#define _QCAR_REFFREEFRAME_GL_H_

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <QCAR/Matrices.h>
#include <QCAR/Vectors.h>

#include <jni.h>

#include "Texture.h"

/// A utility class for the RefFree frame to handle the OpenGL operations
class RefFreeFrameGL
{
public:

    enum TEXTURE_NAME
    {
        TEXTURE_VIEWFINDER_MARKS_PORTRAIT, // Viewfinder in portrait mode
        TEXTURE_VIEWFINDER_MARKS,          // Viewfinder in landscape mode
        TEXTURE_COUNT                      // Not a texture, Count of Predef textures
    };

    RefFreeFrameGL( );
    ~RefFreeFrameGL( );

    /// Init the rendering class with a given screen size
    bool init(int screenWidth, int screeHeight);

    // Load the given textures using utility functions
    bool getTextures(JNIEnv* env, jobject obj);

    /// Quickly set the color for rendering
    void setColor(float r, float g, float b, float a)
    {
        color.data[0] = r;
        color.data[1] = g;
        color.data[2] = b;
        color.data[3] = a;
    }
    void setColor(float c[4])
    {
        color.data[0] = c[0];
        color.data[1] = c[1];
        color.data[2] = c[2];
        color.data[3] = c[3];
    }

    /// Renders the viewfinder
    void renderViewfinder( );

    /// Set the scale for the model view matrix
    void setModelViewScale(float scale)
    {
        modelview.data[14] = scale;
    }

protected:

    /// OpenGL handles for the various shader related variables
    unsigned int shaderProgramID;  /// The Shaders themselves
    GLint vertexHandle;            /// Handle to the Vertex Array
    GLint textureCoordHandle;      /// Handle to the Texture Coord Array
    GLint colorHandle;             /// Handle to the color vector
    GLint mvpMatrixHandle;         /// Handle to the product of the Projection and Modelview Matrices

    /// Projection and Modelview Matrices 
    QCAR::Matrix44F projectionOrtho, modelview;

    /// Color vector
    QCAR::Vec4F color;

    /// Texture names and textures
    static const char* textureNames[RefFreeFrameGL::TEXTURE_COUNT];
    Texture** textures;

    /// Vertices, texture coordinates and vector indices 
    enum
    {
        NUM_FRAME_VERTEX_TOTAL = 4, 
        NUM_FRAME_INDEX = 1 + NUM_FRAME_VERTEX_TOTAL,
    };
    float frameVertices_viewfinder[NUM_FRAME_VERTEX_TOTAL * 3];
    float frameTexCoords[NUM_FRAME_VERTEX_TOTAL * 2];
    unsigned short frameIndices[NUM_FRAME_INDEX];

    /// Portrait/Landscape status detected in init()
    bool isActivityPortrait;

private:
    /// Hidden copy constructor
    RefFreeFrameGL(const RefFreeFrameGL &);

    /// Hidden assignment operator
    RefFreeFrameGL& operator=(const RefFreeFrameGL &);

};

#endif // _QCAR_REFFREEFRAME_GL_H_
