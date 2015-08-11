package com.androidexperiments.tunnelvision.datatextures.canvas;

import android.content.Context;
import android.graphics.Bitmap;

import com.androidexperiments.tunnelvision.datatextures.DataSampler;
import com.androidexperiments.tunnelvision.gl.Texture;

/**
 * Created by kylephillips on 5/27/15.
 */
public class BitmapDataSampler extends DataSampler
{
    @Override
    protected String getFragURI()
    {
        return "textureFs.glsl";
    }

    protected Bitmap mBitmap;
    protected Texture mTexture;
    protected boolean mShouldInterpolate = false;
    protected boolean mShouldRepeat = false;

    public BitmapDataSampler(Context context, Bitmap bitmap, boolean shouldInterpolate, boolean shouldRepeat)
    {
        super(context);
        mShouldInterpolate = shouldInterpolate;
        mShouldRepeat = shouldRepeat;
        mBitmap = bitmap;
    }

    @Override
    public void build()
    {
        mTexture = new Texture(mBitmap, mShouldInterpolate, mShouldRepeat);
        super.build();
    }

    @Override
    public boolean draw()
    {
        if( !getNeedsUpdate() || !isReady() )
        {
            return false;
        }

        int progId = mProgram.getProgramId();
        mFrameBuffer.bind();
        mBuffer.predraw(progId);
        setUniforms();
        mBuffer.draw1(progId, mTexture.getTextureId());

        setNeedsUpdate(true);

        return true;
    }
}
