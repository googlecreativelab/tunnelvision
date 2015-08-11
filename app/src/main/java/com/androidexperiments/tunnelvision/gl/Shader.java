package com.androidexperiments.tunnelvision.gl;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.androidexperiments.shadercam.utils.ShaderUtils;

import java.io.IOException;

/**
 * Created by kylephillips on 5/5/15.
 */
public class Shader
{
    public static final String TAG = Shader.class.getSimpleName();

    int mShaderId;
    boolean mHasLoaded = false;
    boolean mIsCompiled = false;
    private Context mContext;

    private boolean mHasDeleted = false;


    public Shader(Context context){
        this.mContext = context;
    }

    public Shader(Context context, String fileLocation, int type){
        this(context);
        load(fileLocation, type);
    }


    /**
     * has the shader-string been loaded?
     * @return false would mean its a bad file path, or load() hasn't been attempted
     */
    public boolean hasLoaded(){
        return mHasLoaded;
    }

    /**
     * has the shader successfully compiled?
     * @return false would mean its an invalid shader, or load() hasn't been attempted
     */
    public boolean isCompiled(){
        return mIsCompiled;
    }

    /**
     * is the shader ready for use?
     * @return
     */
    public boolean isReady(){
        return hasLoaded() && isCompiled();
    }

    public int getShaderId(){
        return mShaderId;
    }

    /**
     * Load the shaders source file into a shader
     * @param fileLocation
     * @param type either GL_VERTEX_SHADER or GL_FRAGMENT_SHADER
     * @return true if it did correctly load a shader
     */
    public boolean load(String fileLocation, int type) {
        if( mHasLoaded ){
            return false;
        }

        String shaderCode;

        try
        {
            shaderCode = ShaderUtils.getStringFromFileInAssets(mContext, fileLocation, true);
        } catch( IOException e ){
            e.printStackTrace();
            mIsCompiled = false;

            return false;
        }

        mHasLoaded = true;

        mShaderId = GLES20.glCreateShader(type);

        if( mShaderId == 0 ){
            String msg = (type == GLES20.GL_VERTEX_SHADER ? "Vertex" : "Fragment");

            Log.e(TAG, "Error creating shader of type " + msg);
            return false;
        }

        GLES20.glShaderSource(mShaderId, shaderCode);

        GLES20.glCompileShader(mShaderId);

        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(mShaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        mIsCompiled = (compileStatus[0] == GLES20.GL_TRUE);

        if( !mIsCompiled ){
            //cleanup

            String compileError = GLES20.glGetShaderInfoLog(mShaderId);
            Log.e(TAG, compileError);
            GLES20.glDeleteShader(mShaderId);
        }

        return mIsCompiled && mHasLoaded;
    }


    public void delete()
    {
        if(mHasDeleted)
        {
            return;
        }
        GLES20.glDeleteShader(mShaderId);
        mHasDeleted = true;
    }


}
