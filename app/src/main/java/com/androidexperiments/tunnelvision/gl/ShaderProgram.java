package com.androidexperiments.tunnelvision.gl;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by kylephillips on 5/5/15.
 */
public class ShaderProgram
{

    private static final String TAG = ShaderProgram.class.getSimpleName();

    protected boolean mHasDeleted = false;

    protected int mProgId;

    protected String[] attributes = new String[]{
            "position",
            "texCoord"
    };

    protected boolean mIsLinked = false;


    public ShaderProgram( Shader[] shaders )
    {
        mProgId = GLES20.glCreateProgram();

        for( Shader shade : shaders ){
            GLES20.glAttachShader(mProgId, shade.getShaderId());
        }
    }

    public ShaderProgram( Shader vertexShader, Shader fragmentShader ){
        this( new Shader[]{ vertexShader, fragmentShader });
    }



    protected void setAttributes()
    {
        for( int i = 0; i < attributes.length; i++ )
        {
            GLES20.glBindAttribLocation(mProgId, i, attributes[i]);
        }
    }

    public ShaderProgram use()
    {
        GLES20.glUseProgram(mProgId);
        return this;
    }


    public ShaderProgram link() throws RuntimeException
    {

        setAttributes();

        GLES20.glLinkProgram(mProgId);

        final int[] linkStatus = new int[1];

        GLES20.glGetProgramiv(mProgId, GLES20.GL_LINK_STATUS, linkStatus, 0);

        if( linkStatus[0] != GLES20.GL_TRUE ){
            GLES20.glDeleteProgram(mProgId);

            String linkerError = GLES20.glGetProgramInfoLog(mProgId);

            Log.e(TAG, linkerError);
            throw new RuntimeException("Error creating program: ");
        }

        mIsLinked = true;


        return this;
    }


    public int getProgramId(){
        return mProgId;
    }


    public boolean isLinked(){
        return mIsLinked;
    }


    public void delete()
    {
        if(mHasDeleted)
        {
            return;
        }
        GLES20.glDeleteProgram(mProgId);
        mHasDeleted = true;
    }
}
