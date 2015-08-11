precision mediump float;


//WEBGL Max is 16
const int MAX_NUM_FRAMES = 16;

uniform vec3 u_offsets[MAX_NUM_FRAMES];

varying vec2 v_texCoord;

uniform vec2 u_resolution;

//uniform vec2 u_offset;

/*uniform float u_minScale;
uniform int u_numFrames;*/

int u_numFrames = 15;
uniform float u_scale;


float map( float value, float start1, float stop1, float start2, float stop2 ){
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}


int getSlitIndex( vec2 texCoord, float nearEdge, float farEdge, float mag, out vec2 pos, out vec2 range)
{
    float localMag;
    for( int i=0; i<u_numFrames; i++ ){


        localMag = mag * float(u_numFrames - i);
        vec3 off = u_offsets[i] * vec3(0.2, 0.0, 0.2);
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

    vec2 q = gl_FragCoord.xy / u_resolution.xy;

    float numFramesf = float(u_numFrames);
    //the left edge of the inner-most slit, normalized
    float nearEdge = (1.0 - u_scale) / 2.0;
    //the right edge of the inner-most slit, normalized
    float farEdge = nearEdge + u_scale;
    //the normalized percentage of screen per slit
    float mag = nearEdge / numFramesf;

    vec2 pos = vec2(0,0);
    vec2 range = vec2(0, 0);
    int i = getSlitIndex(q, nearEdge, farEdge, mag, pos, range);

    //vec2 texCoord = v_texCoord;//getTexCoord(v_texCoord, currScale);//pos, currScale);
    float x = map(pos.x, range.x, range.y, 0.0, 1.0);
    float y = map(pos.y, range.x, range.y, 0.0, 1.0);
    vec2 texCoord = vec2(x,y);

    float inc = 1.0 / 255.0;

    float idx = float(i) * inc + inc;

    float percent = 1.0 * (float(u_numFrames - i) / float(u_numFrames));


    gl_FragColor = vec4(texCoord.xy, float(i) * inc + inc, 1.0 - percent);
}
