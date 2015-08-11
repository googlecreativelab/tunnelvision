package com.androidexperiments.tunnelvision.datatextures;

import android.content.Context;
import android.opengl.GLES20;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by kylephillips on 5/27/15.
 */
public class TunnelDataSampler extends DataSampler
{
    protected static final String TAG =  TunnelDataSampler.class.getSimpleName();

    public static final int MAX_SLITS = 15;

    @Override
    public Resolution getRequiredResolution()
    {
        //these have no benefit to a higher-res FrameBuffer
        return Resolution.LOW;
    }

    @Override
    public boolean hasRepeatingRGChannels()
    {
        //use the v_texCoord attribute instead
        return false;
    }

    protected float[][] mOffsetHistory;
    protected float[] offsets;

    protected int mTouchCount = 0;
    protected float[] mStartTouch = new float[3];
    protected float[] mCurrentTouch = new float[3];
    private float[] mTouchDiff = new float[3];

    @Override
    protected String getFragURI()
    {
        return "tunnelFs.glsl";
    }

    public TunnelDataSampler( Context context )
    {
        super(context);
        setScale(0.2f);

        mOffsetHistory = new float[MAX_SLITS][3];

        for( int i=0; i<mOffsetHistory.length; i++ ){
            mOffsetHistory[i] = new float[3];
            for( int j=0; j<mOffsetHistory[i].length; j++ )
            {
                mOffsetHistory[i][j] = 0f;
            }
        }

    }


    @Override
    public boolean onTouch(View view, MotionEvent event)
    {
        super.onTouch(view, event);

/*        if( mScaleDetector.isInProgress() )
        {
            return true;
        }*/
        //normalized touch between 0-1.0
        float x = event.getX() / (float)view.getWidth();
        float y = event.getY() / (float)view.getHeight();

        switch( event.getAction() ) {
            case MotionEvent.ACTION_DOWN:
                mStartTouch[0] = x;
                mStartTouch[1] = y;
                mStartTouch[2] = 1f;
                break;

            case MotionEvent.ACTION_MOVE:
                mTouchCount++;
                mCurrentTouch[0] = x;
                mCurrentTouch[1] = y;
                mCurrentTouch[2] = 1f;

                //update the reference every 3 touches to slow it down,
                //but update the recent values every time
                if( mTouchCount % 3 == 0 )
                {
                    diffTouches();
                } else {
                    diffTouches();
                }
                break;
        }


        return true;
    }


    private void diffTouches()
    {
        mTouchDiff[0] = (mCurrentTouch[0] - mStartTouch[0]) * -1;
        mTouchDiff[1] = 0f;
        mTouchDiff[2] = (mCurrentTouch[1] - mStartTouch[1]);

        mTouchDiff[0] *= 2f;
        mTouchDiff[2] *= 2f;
    }



    protected void updateOffsets(float x, float y, float z){
        float[] first = mOffsetHistory[0];

        for(int i=0; i<mOffsetHistory.length-1; i++ )
        {
            mOffsetHistory[i] = mOffsetHistory[i+1];
        }

        first[0] = x;
        first[1] = y;
        first[2] = z;

        mOffsetHistory[mOffsetHistory.length-1] = first;
    }

    @Override
    public void setUniforms()
    {
        super.setUniforms();

        updateOffsets(mTouchDiff[0], mTouchDiff[1], mTouchDiff[2]);

        offsets = new float[mOffsetHistory.length * mOffsetHistory[0].length];

        int offsetHistoryLocation = GLES20.glGetUniformLocation(getProgramId(), "u_offsets");

        //flatten
        for( int i=0; i<mOffsetHistory.length; i++ ){
            int j = i*3;
            offsets[j] = mOffsetHistory[i][0];
            offsets[j+1] = mOffsetHistory[i][1];
            offsets[j+2] = mOffsetHistory[i][2];
        }

        GLES20.glUniform3fv(offsetHistoryLocation, mOffsetHistory.length, offsets, 0);
        setNeedsUpdate(true);
    }
}

