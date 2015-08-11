package com.androidexperiments.tunnelvision.datatextures;

import android.content.Context;

/**
 * Created by kylephillips on 6/9/15.
 */
public class VerticalDataSampler extends DataSampler
{


    @Override
    public Resolution getRequiredResolution()
    {
        //no benefit to a higher-res FrameBuffer
        return Resolution.LOW;
    }

    @Override
    public boolean hasRepeatingRGChannels()
    {
        //use the v_texCoord attribute instead
        return false;
    }

    @Override
    protected String getFragURI()
    {
        return "verticalFs.glsl";
    }


    public VerticalDataSampler(Context context)
    {
        super(context);
        setScale(0.01f);
    }

}
