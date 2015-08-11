precision mediump float;

varying vec2 v_texCoord;

uniform vec2 u_resolution;

int u_numFrames = 15;

int u_columns = 10;
int u_rows = 15;

float map( float value, float start1, float stop1, float start2, float stop2 ){
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}

void main() {
    vec2 q = gl_FragCoord.xy / u_resolution.xy;

    float cellWidth = 1.0 / float(u_columns);
    float cellHeight = 1.0 / float(u_rows);

    float x = cellWidth - mod(q.x, cellWidth);
    float y = cellHeight - mod(q.y, cellHeight);

    int i = int( q * float(u_rows) ) + 2;

    gl_FragColor = vec4(1.0 - (x * float(u_columns)), 1.0 - (y * float(u_rows)), (1.0/255.0) * float(i), 1.0);

}