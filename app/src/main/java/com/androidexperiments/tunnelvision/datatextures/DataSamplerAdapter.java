package com.androidexperiments.tunnelvision.datatextures;

import android.content.Context;

import com.androidexperiments.tunnelvision.datatextures.canvas.NoiseBitmapDataSampler;

/**
 * Created by kylephillips on 5/28/15.
 */
public class DataSamplerAdapter
{

    public enum Sampler {
        TUNNEL_REPEAT,
        NOISE,
        TUNNEL,
        NOISE_BITMAP,
        VERTICAL,
        TWIRL
    }

    protected int i = 0;
    protected Context mContext;
    protected int mResX;
    protected int mResY;

    public DataSamplerAdapter(Context context, int resX, int resY)
    {
        mContext = context;
        mResX = resX;
        mResY = resY;

    }

    public DataSampler get( int i )
    {
        return get(Sampler.values()[i]);
    }

    public DataSampler get( Sampler selection )
    {
        switch( selection )
        {

            case TWIRL:
                return new TwirlDataSampler(mContext);

            case VERTICAL:
                return vertical(mContext);

            case TUNNEL:
                return tunnel(mContext);

            case NOISE:
                return noise2(mContext, mResX, mResY);

            case NOISE_BITMAP:
                return noiseSampler(mContext, mResX/16, mResY/16);

            case TUNNEL_REPEAT:
            default:
                return tunnelRepeat(mContext);
        }

    }

    public DataSampler next()
    {
        Sampler[] samplers = Sampler.values();
        return get(samplers[i++%samplers.length]);
    }

    public DataSampler noise2(Context context, int resX, int resY)
    {
        return new NoiseDataSampler(context);
    }

    public DataSampler noiseSampler(Context context, int width, int height)
    {
        return new NoiseBitmapDataSampler(context, width, height);
    }


    public DataSampler tunnel(Context context)
    {
        return new TunnelDataSampler(context);
    }

    public DataSampler tunnelRepeat(Context context)
    {
        return new TunnelRepeatDataSampler(context);
    }

    public DataSampler vertical(Context context)
    {
        return new VerticalDataSampler(context);
    }

}
