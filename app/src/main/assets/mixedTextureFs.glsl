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

varying vec2 v_texCoord;


uniform bool u_useTexCoord;
uniform int u_numFrames;


void main() {


    //Skip the shader and just show the data texture
/*    vec4 c = texture2D(u_image1, v_texCoord.xy);
    c.b = c.b * 15.0;

    gl_FragColor = vec4(c.b, c.b, c.b, 1.0);
    return;*/

  //the lower this number the closer to black, normalized
  float brightness = 1.0;

  float range = (texture2D(u_image1, v_texCoord.xy).b * 255.0);//getSlitIndex(v_texCoord, nearEdge, farEdge, mag, pos, range);

  int i = int(range);

  if( fract(range) >= 0.5 ){
    i += 1;
  }

  vec4 samp = texture2D(u_image1, v_texCoord);
  vec2 texCoord = samp.xy;

  if( u_useTexCoord ) {
    texCoord = v_texCoord;
  }
  brightness = samp.w;

  vec4 color = vec4(0.0,0.0,0.0,1.0);


    if( i == 0 ){
        color = texture2D(u_image2, v_texCoord); //pos.xy); // + ((texCoord.xy - 0.5) * currScale));
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
        color = texture2D(u_image16, texCoord);
    }


  gl_FragColor = color * brightness;
}
