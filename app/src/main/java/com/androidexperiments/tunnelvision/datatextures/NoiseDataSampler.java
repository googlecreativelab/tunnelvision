package com.androidexperiments.tunnelvision.datatextures;

import android.content.Context;
import android.opengl.GLES20;
import android.view.MotionEvent;
import android.view.View;

import java.util.Date;

/**
 * Created by kylephillips on 5/28/15.
 */
public class NoiseDataSampler extends DataSampler
{


    @Override
    public Resolution getRequiredResolution()
    {
        //this shader is more intensive and should use a low-resolution framebuffer
        return Resolution.LOW;
    }

    protected float px = 0;
    protected float py = 0;
    float x = 0;
    float y = 0;

    protected float[] touch = new float[]{ 0f, 0f, 0f };


    @Override
    public boolean hasRepeatingRGChannels()
    {
        //use the v_texCoord attribute instead
        return false;
    }

    @Override
    protected String getFragURI()
    {
        return "noiseFs.glsl";
    }

    public NoiseDataSampler(Context context)
    {
        super(context);
        setScale(0.5f);
    }


    @Override
    public boolean onTouch(View view, MotionEvent event)
    {
        super.onTouch(view, event);

        if( mScaleDetector.isInProgress() )
        {
            return true;
        }

        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                px = event.getRawX() / (float)view.getWidth();
                py = event.getRawY() / (float)view.getHeight();
                //do not break

            case MotionEvent.ACTION_MOVE:
                x = event.getRawX() / (float)view.getWidth();
                y = event.getRawY() / (float)view.getHeight();
                touch[0] += x - px;
                touch[1] += y - py;
                touch[2] = 1f;

                px = x;
                py = y;
                setNeedsUpdate(true);
        }

        return true;
    }

    @Override
    protected void setUniforms()
    {
        super.setUniforms();

        int mOffsetLoc = GLES20.glGetUniformLocation(getProgramId(), "u_offset");
        GLES20.glUniform3f(mOffsetLoc, touch[0], touch[1], touch[2]);

    }
}
