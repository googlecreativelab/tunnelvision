package com.androidexperiments.tunnelvision.datatextures;

import android.content.Context;
import android.opengl.GLES20;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.androidexperiments.tunnelvision.gl.PlaneRenderer;
import com.androidexperiments.tunnelvision.gl.FrameBuffer;
import com.androidexperiments.tunnelvision.gl.Shader;
import com.androidexperiments.tunnelvision.gl.ShaderProgram;

/**
 * Created by kylephillips on 5/27/15.
 *
 * DataSampler represents the process of making an OpenGL based texture
 * used for data. DataSampler anticipates having `draw()` called 60fps and relies on
 * getNeedsUpdate() to determine if it should run through the shader program again.
 *
 * Any Class inheriting should override getFragURI() and potentially getVertURI(), setUniforms()
 * and draw(). Inside `setUniforms()` is a good place to use setNeedsUpdate(true) if required
 * by your `ShaderProgram`
 *
 * The data is as follows:
 *
 * Red = uv x-coordinate
 * Green = uv y-coordinate
 * Blue = index for slice of time
 * Alpha = brightness value
 *
 */
public class DataSampler implements View.OnTouchListener
{

    public enum Resolution {
        LOW, HIGH
    }

    /**
     * what scale of resoltuion does this DataSampler require?
     * @return
     */
    public Resolution getRequiredResolution(){
        return Resolution.HIGH;
    }


    /**
     * do the sampler RG channels repeat?
     * if not we can achieve higher-resolution in our composite shader
     * by just using its v_texCoord varying;
     */
    public boolean hasRepeatingRGChannels()
    {
        return true;
    }

    /**
     * does the texture need to be re-rendered?
     * @return
     */
    public boolean getNeedsUpdate() {
        return mNeedsUpdate;
    }

    /**
     * set whether the texture needs to be re-rendered,
     * most-likely if a uniform has changed.
     * @param mNeedsUpdate
     */
    public void setNeedsUpdate(boolean mNeedsUpdate) {
        this.mNeedsUpdate = mNeedsUpdate;
    }

    /**
     * has something changed requiring the texture to be updated?
     */
    private boolean mNeedsUpdate = true;

    protected int mUniformResolutionLocation;
    protected int mUniformScaleLocation;

    public float getScale()
    {
        return mScale;
    }

    public void setScale(float scale)
    {
        scale = clampScale(scale);
        if( !mNeedsUpdate )
        {
            mNeedsUpdate = scale != getScale();
        }
        mScale = scale;
    }

    protected float mScale = 1.0f;

    protected String getVertURI()
    {
        return "baseVs.glsl";
    }

    protected String getFragURI()
    {
        return "baseFs.glsl";
    }

    protected ShaderProgram mProgram;

    protected PlaneRenderer mBuffer;

    protected ScaleGestureDetector mScaleDetector;

    public FrameBuffer getFrameBuffer()
    {
        return mFrameBuffer;
    }

    public void setFrameBuffer(FrameBuffer frameBuffer)
    {
        //set that it needs to update if it already did or if
        //this is a new framebuffer that was set
        setNeedsUpdate(getNeedsUpdate() || getFrameBuffer() != frameBuffer);
        mFrameBuffer = frameBuffer;
    }

    public PlaneRenderer getBuffer()
    {
        return mBuffer;
    }

    public void setBuffer(PlaneRenderer buffer)
    {
        mBuffer = buffer;
    }

    protected FrameBuffer mFrameBuffer;

    protected Context mContext;

    public DataSampler(Context context)
    {
        mContext = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void build()
    {
        mProgram = new ShaderProgram(
                new Shader(mContext, getVertURI(), GLES20.GL_VERTEX_SHADER),
                new Shader(mContext, getFragURI(), GLES20.GL_FRAGMENT_SHADER)
        );

        mProgram.link();
    }

    public boolean isBuilt()
    {
        return mProgram != null && mProgram.isLinked();
    }

    public int getProgramId()
    {
        return mProgram.getProgramId();
    }

    protected void setUniforms()
    {
        mUniformResolutionLocation = GLES20.glGetUniformLocation(getProgramId(), "u_resolution");
        mUniformScaleLocation = GLES20.glGetUniformLocation(getProgramId(), "u_scale");
        if( mUniformResolutionLocation >= 0 )
        {
            GLES20.glUniform2f(mUniformResolutionLocation, (float) mFrameBuffer.getWidth(), (float) mFrameBuffer.getHeight());
        }

        if( mUniformScaleLocation >= 0 )
        {
            GLES20.glUniform1f(mUniformScaleLocation, transformScale(mScale));
        }
    }

    public boolean isReady()
    {
        return mFrameBuffer != null && mBuffer != null && mProgram != null && mProgram.isLinked();
    }

    public boolean draw()
    {
        if( !isReady() || !mNeedsUpdate )
        {
            return false;
        }

        setNeedsUpdate(false);

        mFrameBuffer.bind();
        mBuffer.predraw(getProgramId());
        //set uniforms may setNeedsUpdate(true)
        setUniforms();
        mBuffer.drawArrays();
        return true;
    }


    public boolean onTouch(View view, MotionEvent event)
    {
        mScaleDetector.onTouchEvent(event);
        return true;
    }


    /**
     * Clamp the range of possible scales as it is set with setScale.
     * @param inScale
     * @return
     */
    protected float clampScale( float inScale )
    {
        return Math.max(0.005f, Math.min(inScale, 1.0f));
    }

    /**
     * Provided a scale, transform the value for best use in the shader. Used at time of applying to uniform.
     * For example, enforce a minimum and maximum value.
     * @param inScale
     * @return
     */
    protected float transformScale( float inScale ){
        return inScale;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = getScale() * detector.getScaleFactor();
            setScale(scale);

            return true;
        }
    }

}
