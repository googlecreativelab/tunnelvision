package com.androidexperiments.tunnelvision.datatextures;

import android.content.Context;

/**
 * Created by kylephillips on 5/27/15.
 */
public class TunnelRepeatDataSampler extends TunnelDataSampler
{

    @Override
    public boolean hasRepeatingRGChannels()
    {
        //use the v_texCoord attribute instead
        return true;
    }
    protected String getFragURI()
    {
        return "tunnelRepeatFs.glsl";
    }

    public TunnelRepeatDataSampler(Context context)
    {
        super(context);
    }

}
