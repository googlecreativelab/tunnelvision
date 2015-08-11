precision mediump float;
uniform sampler2D u_image1;
varying vec2 v_texCoord;

uniform vec2 u_resolution;

uniform float u_scale;

void main(){

    vec2 q = gl_FragCoord.xy / u_resolution.xy;

    vec4 texel = texture2D(u_image1, q * max(u_scale, 0.01));

    float b = 1.0 / 255.0 * 1.0;
    gl_FragColor = vec4(q.xy, max(texel.b, 0.004), 1.0);
}
