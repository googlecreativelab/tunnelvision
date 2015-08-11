precision mediump float;


//WEBGL Max is 16
const int MAX_NUM_FRAMES = 16;

uniform sampler2D u_image1;
uniform sampler2D u_image2;
uniform sampler2D u_image3;
uniform sampler2D u_image4;
uniform sampler2D u_image5;
uniform sampler2D u_image6;
uniform sampler2D u_image7;
uniform sampler2D u_image8;
uniform sampler2D u_image9;
uniform sampler2D u_image10;
uniform sampler2D u_image11;
uniform sampler2D u_image12;
uniform sampler2D u_image13;
uniform sampler2D u_image14;
uniform sampler2D u_image15;
uniform sampler2D u_image16;

uniform vec3 offsets[MAX_NUM_FRAMES];

varying vec2 v_texCoord;

//uniform vec2 u_offset;

uniform float u_minScale;
uniform int u_numFrames;

float average( vec3 c ){
    return (c.r + c.g + c.b) / 3.0;
}

float map( float value, float start1, float stop1, float start2, float stop2 ){
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}

vec2 getTexCoord( vec2 texCoord, float currScale ){
  //return (texCoord.xy - 0.5) * (1.0 / currScale);
  return texCoord.xy + ((texCoord.xy - 0.5) * currScale);
}

int getSlitIndex( vec2 texCoord, float nearEdge, float farEdge, float mag, out vec2 pos, out vec2 range)
{
    float localMag;
    for( int i=0; i<u_numFrames; i++ ){


        localMag = mag * float(u_numFrames - i);
        vec3 off = offsets[i] * vec3(0.2, 0.0, 0.2);
        //out
        pos = texCoord.xy + off.xz;


        if( pos.x < nearEdge - localMag ||
            pos.x > farEdge + localMag  ||

            pos.y < nearEdge - localMag ||
                pos.y > farEdge + localMag ){


               range = vec2(nearEdge - localMag, farEdge + localMag);

                return i;
        }
    }

    range = vec2(nearEdge - localMag, farEdge + localMag);

    return u_numFrames-1;
}


void main() {

  vec4 color = vec4(0,0,0,1.0);

  float numFramesf = float(u_numFrames);
  //the left edge of the inner-most slit, normalized
  float nearEdge = (1.0 - u_minScale) / 2.0;
  //the right edge of the inner-most slit, normalized
  float farEdge = nearEdge + u_minScale;
  //the normalized percentage of screen per slit
  float mag = nearEdge / numFramesf;

  //the lower this number the closer to black, normalized
  float brightness = 1.0;

  vec2 pos = vec2(0f,0f);
  vec2 range = vec2(0f, 0f);
  int i = getSlitIndex(v_texCoord, nearEdge, farEdge, mag, pos, range);

  float currScale = u_minScale + (mag * float(i));




    //vec2 texCoord = v_texCoord;//getTexCoord(v_texCoord, currScale);//pos, currScale);
    float x = map(pos.x, range.x, range.y, 0.0, 1.0);
    float y = map(pos.y, range.x, range.y, 0.0, 1.0);
    vec2 texCoord = vec2(x,y);

    //darkest point in the center
    //brightness = 1.0 - (1.0/numFramesf) * float(i);
    //darkest point on the edges
    brightness = (1.0/numFramesf) * float(i);

    brightness = u_minScale + (brightness*(1.0-u_minScale));


    if( i == 0 ){
        color = texture2D(u_image1, v_texCoord); //pos.xy); // + ((texCoord.xy - 0.5) * currScale));
    } else if( i == 1 ){
        color = texture2D(u_image2, texCoord);
    } else if( i == 2 ){
        color = texture2D(u_image3, texCoord);
    } else if( i == 3 ){
        color = texture2D(u_image4, texCoord);
    } else if( i == 4 ){
        color = texture2D(u_image5, texCoord);
    } else if( i == 5 ){
        color = texture2D(u_image6, texCoord);
    } else if( i == 6 ){
        color = texture2D(u_image7, texCoord);
    } else if( i == 7 ){
        color = texture2D(u_image8, texCoord);
    } else if( i == 8 ){
        color = texture2D(u_image9, texCoord);
    } else if( i == 9 ){
        color = texture2D(u_image10, texCoord);
    } else if( i == 10 ){
        color = texture2D(u_image11, texCoord);
    } else if( i == 11 ){
        color = texture2D(u_image12, texCoord);
    } else if( i == 12 ){
        color = texture2D(u_image13, texCoord);
    } else if( i == 13 ){
        color = texture2D(u_image14, texCoord);
    } else if( i == 14 ){
        color = texture2D(u_image15, texCoord);
    } else {
        //color = texture2D(u_image16, v_texCoord.xy * (1.0 / currScale));
        color = texture2D(u_image16, texCoord);
    }


  //gl_FragColor = vec4( mag, 0f, 0f, 1f );
  //gl_FragColor = vec4((1.0/float(u_numFrames)) * float(i), 0f, 0f, 1f);//color * brightness;
  gl_FragColor = color * brightness;
  //gl_FragColor = vec4(v_texCoord.x, v_texCoord.y, 0f, 1f);
}
