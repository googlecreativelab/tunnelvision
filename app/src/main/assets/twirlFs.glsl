precision mediump float;
#define PI 3.1415926535897932384626433832795

const float TAU = 2.0 * PI;
const int MAX_NUM_CIRCLES = 24;
const int MAX_RINGS = 15;

varying vec2 v_texCoord;

//the render-targets resolution
uniform vec2 u_resolution;
//the number of sides to the poly
uniform int u_polyResolution;
//the scale of the middle poly
uniform float u_scale;
//radian value for twisting per ring
uniform float u_twist;


float radius = 0.18;





float map( in float value, in float start1, in float stop1, in float start2, in float stop2 ){
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}

//get the radius of the current angle clamped to an N-sided polygon
float getLengthForAngleOnNPolygon( in int n, in float angle ){

    float fn = float(n);

    if( angle < 0.0 ){
        angle = angle += TAU;
    }

    float interval = TAU / fn;
    float c = cos(PI / fn);

    angle = mod(angle, interval);

    float s = cos( angle - (PI / fn) );

    return c / s;
}


// get the index of the concentric ring the current radius belongs to
int getRingIndex( in float r, in float angle, out float startRadius ){

    float len = getLengthForAngleOnNPolygon(u_polyResolution, angle);

    for( int i=0; i<MAX_RINGS; i++ ){
        if( i > 0 ){
            startRadius = u_scale + radius * float(i-1);
        } else {
            startRadius = 0.0;
        }
		float localRad = radius;

		if( i == 0 ){
		    localRad = u_scale;
		}

        localRad = localRad * len;

        if( r <= startRadius + localRad ){
            return i;
        }
    }
    return 0;
}

//get index for its repition around the current concentric ring
//also get the start and end of that arc
int getNumCircleAroundRing( in float angle, in float twist, in int numCircles, out float arcStart, out float arcEnd ){

    //the radius of a single-circle around its series
    float individualRadius = TAU / float(numCircles);

    for( int i=0; i<MAX_NUM_CIRCLES; i++ ){
        float fi = float(i);

        if( i >= numCircles ){
            break;
        }

        //the outs
        arcStart = individualRadius * fi + twist;
        arcEnd = individualRadius * (fi+1.0) + twist;

        //if the arc surpasses TAU then wrap the angle to being larger as well
        if( arcStart > PI && arcEnd > TAU ){
            if( angle < PI ){
                angle += TAU;
            }
        }

        if( angle >= arcStart && angle <= arcEnd ){
            return i;
        }
    }

    return 0;
}


void main()
{
    float firstRadius = u_scale;

    vec4 color = vec4(0.0);

	vec2 uv = gl_FragCoord.xy / u_resolution.xy;
    vec2 p = -2.0 + 4.0 * uv;
    uv = -2.0 + 4.0 * uv;

    float aspect = u_resolution.x / u_resolution.y;
    float invAspect = u_resolution.y / u_resolution.x;
    p.x *= aspect;

    float angle = atan(p.y, p.x);
    float r = length(p);

    if( angle < 0.0 ){
        angle = angle + TAU;
    }

    float startRadius = 0.0;
    int i = getRingIndex(r, angle, startRadius);


    if( i > 0 ){
    } else {
        radius = firstRadius;
    }

    float fi = float(i);
    float twistOffset = u_twist * fi;
    float arcStart = 0.0;
    float arcEnd = 0.0;
    int j = getNumCircleAroundRing(angle, twistOffset, u_polyResolution, arcStart, arcEnd);

    if( arcStart > PI && arcEnd > TAU && angle < PI ){
        angle += TAU;
    }

    if( i == 0 ){
        //map the center one linearly
        color.r = map(uv.x, -firstRadius * invAspect, firstRadius * invAspect, 0.0, 1.0);
        color.g = map(uv.y, -firstRadius * invAspect, firstRadius * invAspect, 0.0, 1.0);
    } else {
        //map the rest radially
        color.r = map(angle, arcEnd, arcStart, 0.0, 1.0);
        color.g = map(r, startRadius - radius, startRadius + radius, 0.0, 1.0);
    }

    if( i < 0 ){
        color.r = 0.0;
        color.g = 0.0;
    }

    color.b = 1.0 - (1.0 / float(MAX_RINGS) * fi);

    color.b = color.b * (15.0/255.0);

/*    red = 0.0;
    green = 0.0;
    blue = float(j) / float(u_polyResolution) * (15.0/255.0);*/

    float perc = min(fi / float(MAX_RINGS - 6), 1.0);

/*    color.a = (0.2 + (0.8 * (perc*perc)));

    if( i == 0 ){
        color.a = 1.0;
    }*/

    color.a = 1.0;

	gl_FragColor = color;
}
