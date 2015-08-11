package com.androidexperiments.tunnelvision.gl;

import android.opengl.GLES20;

/**
 * Created by kylephillips on 5/5/15.
 */
public class FrameBuffer
{

    private int[] mFBOId = new int[1];
    private int[] mTexId = new int[1];

    private int mWidth;

    public int getHeight()
    {
        return mHeight;
    }

    public int getWidth()
    {
        return mWidth;
    }

    private int mHeight;


    public FrameBuffer( int width, int height ){

        mWidth = width;
        mHeight = height;

        GLES20.glGenFramebuffers(1, mFBOId, 0);

        GLES20.glGenTextures(1, mTexId, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexId[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        //if you want that to be smoothly interpolated change GL_NEAREST to GL_LINEAR
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);//GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);//GLES20.GL_NEAREST);


        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                width,
                height,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null
        );

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOId[0]);

        GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,
                mTexId[0],
                0
        );

    }


    public int getTexture(){
        return mTexId[0];
    }

    public void bind(){
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOId[0]);
    }
}
