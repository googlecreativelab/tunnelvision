precision mediump float;

const int MAX_NUM_FRAMES = 15;

//uniform vec3 u_offsets[MAX_NUM_FRAMES];

varying vec2 v_texCoord;

uniform vec2 u_resolution;
uniform float u_scale;

int numFrames = 15;


float map( float value, float start1, float stop1, float start2, float stop2 ){
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}



void main() {

    vec2 q = gl_FragCoord.xy / u_resolution.xy;

    //float numFramesf = float(numFrames);

    float mapped = map(1.0-u_scale, 0.0, 1.0, 2.0, 15.0);

    numFrames = int(mapped);
    float leftOver = fract(mapped) / mapped;

    //float spacing = (1.0 - u_scale) / (numFramesf-1.0);
    float spacing = 1.0 / float(numFrames);

    int i = 0;


    for( i=0; i<numFrames; i++){
        if( q.y >= 1.0 - (spacing * float(i)) - leftOver ){//float(u_scale + (spacing*float(i))) ) {
            break;
        }
    }

    float inc = 1.0 / 255.0;

    //i is between 0 and numFrames we want an index that is between MAX and (MAX-numFrames)

    float b = map(float(i), 0.0, float(numFrames), float(MAX_NUM_FRAMES), float(MAX_NUM_FRAMES-numFrames));

    gl_FragColor = vec4(q.xy, b * inc, 1.0); //1.0 - percent);
}
