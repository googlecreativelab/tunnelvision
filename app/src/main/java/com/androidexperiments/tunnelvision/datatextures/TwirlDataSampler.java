package com.androidexperiments.tunnelvision.datatextures;

import android.content.Context;
import android.opengl.GLES20;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by kylephillips on 6/15/15.
 */
public class TwirlDataSampler extends DataSampler
{

    float x = 0f;
    float y = 0f;

    float px = 0f;
    float py = 0f;

    int mPolyResolution = 3;
    float mTwist = 0f;

    @Override
    protected String getFragURI()
    {
        return "twirlFs.glsl";
    }


    public TwirlDataSampler(Context context)
    {
        super(context);
        setScale(0.3f);
    }


    protected int calculatePolyResolution( float x )
    {
/*        if( x < 0.15 ){
            return 48;
        }*/
        int min = 3;
        int max = 21;

        float v = ((float)max - min) * x;

        return Math.round(v) + min;
    }

    protected float calculateTwist( float y )
    {
        if( y < 0.15f ){
            return 0.0f;
        }
        float max = (float)Math.PI / 12.0f;
        return max * y;
    }


    @Override
    protected void setUniforms()
    {
        super.setUniforms();

        int polyResLoc = GLES20.glGetUniformLocation(getProgramId(), "u_polyResolution");
        GLES20.glUniform1i(polyResLoc, mPolyResolution);


        int twistLoc = GLES20.glGetUniformLocation(getProgramId(), "u_twist");
        GLES20.glUniform1f(twistLoc, mTwist);
    }


    public void setPolyResolution( int resolution )
    {
        if( !getNeedsUpdate() ){
            setNeedsUpdate( resolution != mPolyResolution );
        }
        mPolyResolution = resolution;
    }

    public void setTwist( float rotation )
    {
        if( !getNeedsUpdate() ){
            setNeedsUpdate( Math.abs(rotation - mTwist) > 0.001 );
        }

        mTwist = rotation;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event)
    {
        super.onTouch(view, event);

        switch(event.getAction())
        {

            case MotionEvent.ACTION_MOVE:
                float cx = event.getRawX() / (float)view.getWidth();
                float cy = event.getRawY() / (float)view.getHeight();
                x += cx - px;
                y += cy - py;
                px = cx;
                py = cy;

                x = Math.min(Math.max(x, 0f), 1f);
                y = Math.min(Math.max(y, 0f), 1f);
                setPolyResolution( calculatePolyResolution(x) );
                setTwist( calculateTwist(y) );


                break;

            case MotionEvent.ACTION_DOWN:
                px = event.getRawX() / (float)view.getWidth();
                py = event.getRawY() / (float)view.getHeight();
        }


        return true;
    }
}
