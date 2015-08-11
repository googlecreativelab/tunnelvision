attribute vec3 position;
attribute vec4 color;
attribute vec2 texCoord;

varying vec2 v_texCoord;


uniform mat4 u_pMatrix;
uniform mat4 u_mvMatrix;
//varying vec4 v_color;


void main() {
   vec3 pos = vec3(position.xy, position.z);
   gl_Position = u_pMatrix * u_mvMatrix * vec4(pos, 1.0);
   v_texCoord = texCoord;
   //v_color = color;
}
