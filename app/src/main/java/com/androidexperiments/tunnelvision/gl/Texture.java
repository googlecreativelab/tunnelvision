package com.androidexperiments.tunnelvision.gl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * Created by kylephillips on 5/5/15.
 */
public class Texture
{

    int[] mTexId = new int[1];

    private boolean mInterpolate = false;
    private boolean mRepeat = false;

    public Texture()
    {
        this(false, false);
    }

    public Texture(boolean shouldInterpolate, boolean shouldRepeat){

        this.mInterpolate = shouldInterpolate;
        this.mRepeat = shouldRepeat;

        GLES20.glGenTextures(1, mTexId, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureId());

        int repeatStrategy = mRepeat ? GLES20.GL_REPEAT : GLES20.GL_CLAMP_TO_EDGE;
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, repeatStrategy);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, repeatStrategy);

        //if you want that to be smoothly interpolated change GL_NEAREST to GL_LINEAR
        int interpStrategy = this.mInterpolate ? GLES20.GL_LINEAR : GLES20.GL_NEAREST;

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, interpStrategy);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, interpStrategy);
    }

    public Texture(Bitmap bitmap, boolean shouldInterpolate, boolean shouldRepeat)
    {
        this(shouldInterpolate, shouldRepeat);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
    }

    public void bind(int textureSpot)
    {
        GLES20.glActiveTexture(textureSpot);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureId());
    }

    public boolean getIsInterpolating(){
        return this.mInterpolate;
    }

    public int getTextureId(){
        return mTexId[0];
    }
}
