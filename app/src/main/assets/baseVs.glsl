attribute vec3 position;
attribute vec2 texCoord;

varying vec2 v_texCoord;
//varying vec4 v_color;

void main() {
   gl_Position = vec4(position.xyz, 1.0);
   v_texCoord = texCoord;
   //v_color = color;
}
