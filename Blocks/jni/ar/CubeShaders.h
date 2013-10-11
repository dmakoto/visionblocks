/*==============================================================================
            Copyright (c) 2012-2013 QUALCOMM Austria Research Center GmbH.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
            
@file 
    CubeShaders.h

@brief
    Defines OpenGL shaders as char* strings.

==============================================================================*/

#ifndef _QCAR_CUBE_SHADERS_H_
#define _QCAR_CUBE_SHADERS_H_

static const char* cubeMeshVertexShader = " \
  \
attribute vec4 vertexPosition; \
attribute vec4 vertexNormal; \
attribute vec2 vertexTexCoord; \
 \
varying vec2 texCoord; \
varying vec4 normal; \
 \
uniform mat4 modelViewProjectionMatrix; \
 \
void main() \
{ \
   gl_Position = modelViewProjectionMatrix * vertexPosition; \
   normal = vertexNormal; \
   texCoord = vertexTexCoord; \
} \
";


static const char* cubeFragmentShader = " \
 \
precision mediump float; \
 \
varying vec2 texCoord; \
varying vec4 normal; \
 \
uniform sampler2D texSampler2D; \
 \
void main() \
{ \
   gl_FragColor = texture2D(texSampler2D, texCoord); \
} \
";

#endif // _QCAR_CUBE_SHADERS_H_
