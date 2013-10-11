/*==============================================================================
             Copyright (c) 2012-2013 QUALCOMM Austria Research Center GmbH.
             All Rights Reserved.
             Qualcomm Confidential and Proprietary

 @file
    RefFreeFrameGL.cpp

 @brief
    Implementation of class RefFreeFrame.

 ==============================================================================*/

// ** Include files
#include <math.h>
#include <string.h>
#include "RefFreeFrameGL.h"
#include "RefFreeFrameNative.h"
#include "SampleUtils.h"
#include <QCAR/Renderer.h>
#include <QCAR/VideoBackgroundConfig.h>
#include <QCAR/Tool.h>
#include <QCAR/CameraDevice.h>

// ** Shaders

static const char
        * frameVertexShader =
                " \
  \
attribute vec4 vertexPosition; \
attribute vec2 vertexTexCoord; \
 \
varying vec2 texCoord; \
 \
uniform mat4 modelViewProjectionMatrix; \
 \
void main() \
{ \
   gl_Position = modelViewProjectionMatrix * vertexPosition; \
   texCoord = vertexTexCoord; \
} \
";

static const char
        * frameFragmentShader =
                " \
 \
precision mediump float; \
 \
varying vec2 texCoord; \
 \
uniform sampler2D texSampler2D; \
uniform vec4 keyColor; \
 \
void main() \
{ \
   vec4 texColor = texture2D(texSampler2D, texCoord); \
   gl_FragColor = keyColor * texColor; \
} \
";

const char *RefFreeFrameGL::textureNames[RefFreeFrameGL::TEXTURE_COUNT] =
{ "viewfinder_crop_marks_portrait.png", "viewfinder_crop_marks_landscape.png" };

RefFreeFrameGL::RefFreeFrameGL( ) :
    shaderProgramID(0), vertexHandle(0), textureCoordHandle(0),
            mvpMatrixHandle(0)
{
    LOG("RefFreeFrameGL Ctor");
    textures = new Texture*[TEXTURE_COUNT];
    for (int i = 0; i < TEXTURE_COUNT; i++)
        textures[i] = 0;
}

RefFreeFrameGL::~RefFreeFrameGL( )
{
    for (int i = 0; i < TEXTURE_COUNT; i++)
        if (textures[i] != 0)
            delete textures[i];

    delete[] textures;
}

bool
RefFreeFrameGL::init(int screenWidth, int screenHeight)
{
    // modelview matrix set to identity
    memset(modelview.data, 0, 16 * sizeof(float));
    modelview.data[0] = modelview.data[5] = modelview.data[10]
            = modelview.data[15] = 1.0f;

    // color is set to pure white
    color.data[0] = color.data[1] = color.data[2] = 1.0f;
    color.data[3] = 0.6f;

    // Detect if we are in portrait mode or not
    isActivityPortrait = (screenWidth < screenHeight);
    
    if ((shaderProgramID = SampleUtils::createProgramFromBuffer(
            frameVertexShader, frameFragmentShader)) == 0)
        return false;

    if ((vertexHandle = glGetAttribLocation(shaderProgramID, "vertexPosition"))
            == -1)
        return false;
    if ((textureCoordHandle = glGetAttribLocation(shaderProgramID,
            "vertexTexCoord")) == -1)
        return false;
    if ((mvpMatrixHandle = glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix")) == -1)
        return false;
    if ((colorHandle = glGetUniformLocation(shaderProgramID, "keyColor")) == -1)
        return false;

    // retrieves the screen size and other video background config values
    QCAR::Renderer &renderer = QCAR::Renderer::getInstance();
    const QCAR::VideoBackgroundConfig &vc = renderer.getVideoBackgroundConfig();

    // makes ortho matrix
    memset(projectionOrtho.data, 0, 16 * sizeof(float));

    // Calculate the Orthograpic projection matrix
    projectionOrtho.data[0] = 2.0f / (float)(vc.mSize.data[0]);
    projectionOrtho.data[5] = 2.0f / (float)(vc.mSize.data[1]);
    projectionOrtho.data[10] = 1.0f / (-10.0f);
    projectionOrtho.data[11] = -5.0f / (-10.0f);
    projectionOrtho.data[15] = 1.0f;

    // Viewfinder size based on the Ortho matrix because it is an Ortho UI element
    // Use the ratio of the reported screen size and the calculated screen size
    // to account for on screen OS UI elements such as the action bar in ICS.
    float sizeH_viewfinder = ((float)screenWidth / vc.mSize.data[0]) * (2.0f
            / projectionOrtho.data[0]);
    float sizeV_viewfinder = ((float)screenHeight / vc.mSize.data[1]) * (2.0f
            / projectionOrtho.data[5]);

    LOG("Viewfinder Size %f %f", sizeH_viewfinder, sizeV_viewfinder);

    // ** initialize the frame with the correct scale to fit the current perspective matrix
    unsigned int cnt = 0, tCnt = 0;

    /**
     * Define the vertices and texture coords for a triangle strip 
     * that will define the Quad where the viewfinder is rendered. 
     *  
     *   0---------1
     *   |         |
     *   |         |
     *   3---------2
     */
    /// Vertex 0
    frameVertices_viewfinder[cnt++] = (-1.0f) * sizeH_viewfinder;
    frameVertices_viewfinder[cnt++] = (1.0f) * sizeV_viewfinder;
    frameVertices_viewfinder[cnt++] = 0.0f;
    frameTexCoords[tCnt++] = 0.0f;
    frameTexCoords[tCnt++] = 1.0f;

    /// Vertex 1
    frameVertices_viewfinder[cnt++] = (1.0f) * sizeH_viewfinder;
    frameVertices_viewfinder[cnt++] = (1.0f) * sizeV_viewfinder;
    frameVertices_viewfinder[cnt++] = 0.0f;
    frameTexCoords[tCnt++] = 1.0f;
    frameTexCoords[tCnt++] = 1.0f;

    /// Vertex 2
    frameVertices_viewfinder[cnt++] = (1.0f) * sizeH_viewfinder;
    frameVertices_viewfinder[cnt++] = (-1.0f) * sizeV_viewfinder;
    frameVertices_viewfinder[cnt++] = 0.0f;
    frameTexCoords[tCnt++] = 1.0f;
    frameTexCoords[tCnt++] = 0.0f;

    /// Vertex 3
    frameVertices_viewfinder[cnt++] = (-1.0f) * sizeH_viewfinder;
    frameVertices_viewfinder[cnt++] = (-1.0f) * sizeV_viewfinder;
    frameVertices_viewfinder[cnt++] = 0.0f;
    frameTexCoords[tCnt++] = 0.0f;
    frameTexCoords[tCnt++] = 0.0f;

    // we also set the indices programmatically 
    cnt = 0;
    for (int i = 0; i < NUM_FRAME_VERTEX_TOTAL; i++)
        frameIndices[cnt++] = i; // one full loop
    frameIndices[cnt++] = 0; // close the loop

    // loads the texture
    for (int i = 0; i < TEXTURE_COUNT; i++)
    {
        if (textures[i])
        {
            glGenTextures(1, &(textures[i]->mTextureID));
            glBindTexture(GL_TEXTURE_2D, textures[i]->mTextureID);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textures[i]->mWidth,
                    textures[i]->mHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                    (GLvoid*)textures[i]->mData);
        }
    }

}

bool
RefFreeFrameGL::getTextures(JNIEnv* env, jobject obj)
{
    bool result = true;
    for (int i = 0; (result && i < TEXTURE_COUNT); i++)
        result = RefFreeFrameNative::getTexture(env, obj, 
                                                textures[i],
                                                textureNames[i]);

    return result;
}

/// Renders the viewfinder
void
RefFreeFrameGL::renderViewfinder()
{
    if (textures == NULL)
        return;

    // Set GL flags
    glEnable ( GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glDisable ( GL_DEPTH_TEST);
    glDisable ( GL_CULL_FACE);

    // Set the shader program
    glUseProgram ( shaderProgramID);
    
    // Calculate the Projection * ModelView matrix and pass to shader
    QCAR::Matrix44F mvp;
    SampleUtils::multiplyMatrix(&(projectionOrtho.data[0]),
            &(modelview.data[0]), &(mvp.data[0]));
    glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE,
            (GLfloat*)&(mvp.data[0]));

    // Set the vertex handle 
    glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0,
            (const GLvoid*)&frameVertices_viewfinder[0]);

    // Set the Texture coordinate handle
    glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0,
            (const GLvoid*)&frameTexCoords[0]);

    // Enable the Vertex and Texture arrays
    glEnableVertexAttribArray ( vertexHandle);
    glEnableVertexAttribArray ( textureCoordHandle);

    // Send the color value to the shader
    glUniform4fv(colorHandle, 1, (GLfloat*)&(color.data[0]));

    // Depending on if we are in portrait or landsacape mode,
    // choose the proper viewfinder texture
    if (isActivityPortrait && textures[TEXTURE_VIEWFINDER_MARKS_PORTRAIT])
    {
        glActiveTexture ( GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,
                textures[TEXTURE_VIEWFINDER_MARKS_PORTRAIT]->mTextureID);
    }
    else if(!isActivityPortrait && textures[TEXTURE_VIEWFINDER_MARKS])
    {
        glActiveTexture ( GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,
                textures[TEXTURE_VIEWFINDER_MARKS]->mTextureID);
    }

    // Draw the viewfinder
    glDrawElements(GL_TRIANGLE_STRIP, NUM_FRAME_INDEX, GL_UNSIGNED_SHORT,
            (const GLvoid*)&frameIndices[0]);

    
    glDisable(GL_BLEND);
    glEnable(GL_DEPTH_TEST);

}


