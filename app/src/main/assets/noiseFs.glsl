precision highp float;

varying vec2 v_texCoord;

uniform vec2 u_resolution;

uniform float u_scale;
uniform vec3 u_offset;

vec3 hash3( vec2 p )
{
    vec3 q = vec3( dot(p,vec2(127.1,311.7)),
				   dot(p,vec2(269.5,183.3)),
				   dot(p,vec2(419.2,371.9)) );
	return fract(sin(q)*43758.5453);
}

float iqnoise( in vec2 x, float u, float v )
{
    vec2 p = floor(x);
    vec2 f = fract(x);

	float k = 1.0+63.0*pow(1.0-v,4.0);

	float va = 0.0;
	float wt = 0.0;
    for( int j=-2; j<=2; j++ )
    for( int i=-2; i<=2; i++ )
    {
        vec2 g = vec2( float(i),float(j) );
		vec3 o = hash3( p + g )*vec3(u,u,1.0);
		vec2 r = g - f + o.xy;
		float d = dot(r,r);
		float ww = pow( 1.0-smoothstep(0.0,1.414,sqrt(d)), k );
		va += o.z*ww;
		wt += ww;
    }

    return va/wt;
}

void main( void ) {

	//vec2 position = ( gl_FragCoord.xy / resolution.xy ) + mouse / 4.0;


	vec2 q = v_texCoord.xy * 2.0;
	//vec2 q = gl_FragCoord.xy / u_resolution.xy;

    vec2 p = 0.5 - 0.5*sin( vec2(1.01,1.01) );
    p = p * 0.65;

    p = vec2(0.5 - 0.5 * -1.0) + (p * 0.25);

    float off = 0.0;
    float offX = clamp(u_offset.x, 0.115, 0.72) * 2.0 - 0.5;

    vec2 pan = vec2(u_offset.x, -u_offset.y) * 8.0;

    float scale = (1.0-u_scale) * 30.0;

	float f = iqnoise( -pan + scale * q, -(offX * 2.0) + p.x + off, p.y + off);

	float r = sqrt( dot(p, p) );

	r = 1.0-r;

	if( r > 0.0 ){
		r = q.x;
	}

    float interval = (255.0/15.0);

    float b = f / interval + (1.0/255.0);

	gl_FragColor = vec4(q.x, q.y, b, 1.0);//(b * 15.0) * 0.6 + 0.4); // + ((1.0/255.0) * 15.0));

}
