package com.androidexperiments.tunnelvision.datatextures.canvas;

import android.content.Context;
import android.opengl.GLES20;
import android.view.MotionEvent;
import android.view.View;

import com.androidexperiments.tunnelvision.datatextures.NoiseSampler;

/**
 * Created by kylephillips on 6/9/15.
 */
public class NoiseBitmapDataSampler extends BitmapDataSampler
{

    float px = 0f;
    float py = 0f;
    float x = 0f;
    float y = 0f;

    @Override
    public boolean hasRepeatingRGChannels()
    {
        //use the v_texCoord attribute instead
        return false;
    }


    @Override
    protected String getFragURI()
    {
        return "texturePanFs.glsl";
    }

    public NoiseBitmapDataSampler(Context context, int width, int height )
    {
        super(context, new NoiseSampler().toBitmap(width, height), true, true);
        setScale(0.5f);
    }

    @Override
    protected float clampScale( float inScale )
    {
        //invert the scale
        return Math.max(0.0f, Math.min(inScale, 0.9f) );
    }

    @Override
    protected float transformScale( float inScale ){
        //inverse the scaling
        return 1.0f - inScale;
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
                break;

            case MotionEvent.ACTION_DOWN:
                px = event.getRawX() / (float)view.getWidth();
                py = event.getRawY() / (float)view.getHeight();
        }

        return true;
    }


    @Override
    protected void setUniforms()
    {
        super.setUniforms();

        int offsetLoc = GLES20.glGetUniformLocation(getProgramId(), "u_offset");

        GLES20.glUniform2f(offsetLoc, -x, y);

        setNeedsUpdate(true);
    }
}
