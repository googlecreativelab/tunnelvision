#extension GL_OES_EGL_image_external : require

//necessary
precision mediump float;
uniform samplerExternalOES camTexture;
uniform float aspectRatio;

varying vec2 v_CamTexCoordinate;
varying vec2 v_TexCoordinate;

uniform float time;


uniform vec2 u_resolution;

uniform sampler2D color;

float map(float value, float start1, float stop1, float start2, float stop2)
{
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}

void main ()
{
    //vec2 uv = v_CamTexCoordinate * norm;
    //set initial color of camera
    //vec4 cameraColor = texture2D(camTexture, v_CamTexCoordinate * vec2(aspectRatio, 1.0));
    vec2 textureCoordinate = v_CamTexCoordinate; // * vec2(aspectRatio, 1.0);

    //figure 8
    //float scale = 2. / (3. - cos(2.*time));
    //float x =  cos(time);
    //float y = sin(2.*time) / 2.;

    gl_FragColor = texture2D(camTexture, textureCoordinate);
}