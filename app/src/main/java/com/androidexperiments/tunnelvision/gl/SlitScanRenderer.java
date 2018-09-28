package com.androidexperiments.tunnelvision.gl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;

import com.androidexperiments.shadercam.fragments.CameraFragment;
import com.androidexperiments.shadercam.fragments.VideoFragment;
import com.androidexperiments.shadercam.gl.CameraRenderer;
import com.androidexperiments.shadercam.gl.VideoRenderer;
import com.androidexperiments.tunnelvision.datatextures.DataSampler;

import java.lang.ref.WeakReference;


/**
 * SlitScanRenderer is responsible for maintaining FrameBuffers of camera-images
 * over time as well as a `DataSampler` as a means for how to render the time
 * in a shader program.
 */
public class SlitScanRenderer extends VideoRenderer
{

    private static final String TAG = SlitScanRenderer.class.getSimpleName();

    WeakReference<Context> mContextWeakReference;

    public static final int MAX_SLITS = 16;

    protected int mTextureWidth;
    protected int mTextureHeight;

    //shader programs
    protected ShaderProgram blackProgram;
    protected ShaderProgram slitScanProgram;

    protected PlaneRenderer mPlaneRenderer;

    protected FrameBuffer mSamplerBufferHigh;
    protected FrameBuffer mSamplerBufferLow;
    protected FrameBuffer[] fbos;

    protected int mFramesBetweenUpdate = 1;
    public void setNumFramesBetweenUpdate(int delay)
    {
        this.mFramesBetweenUpdate = Math.max(delay, 1);
    }
    public int getNumFramesBetweenUpdate()
    {
        return this.mFramesBetweenUpdate;

    }

    protected int mNumSlits;

    /**
     * Enable the target texture to be a scaled version of the source image
     */
    protected float mTextureScale = 1f;

    protected int mFrameCount = 0;

    protected int[] mFBOTextures;



    /**
     * Construct a new SlitScanRenderer
     * @param context
     * @param numSlits
     */
    public SlitScanRenderer(Context context, VideoFragment cameraFragment, int numSlits)
    {
        super(context);
        setVideoFragment(cameraFragment);
        mNumSlits = numSlits;
        mContextWeakReference = new WeakReference<>(context);

    }

    public DataSampler getSampler()
    {
        return mSampler;
    }

    public void setSampler(DataSampler sampler)
    {
        mSampler = sampler;
    }

    protected DataSampler mSampler;

    @Override
    protected void onSetupComplete()
    {
        //return to the window-system-provided framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        super.onSetupComplete();

        //attach a shader that just paints everything black
        Shader baseVs = new Shader(mContextWeakReference.get(), "baseVs.glsl", GLES20.GL_VERTEX_SHADER);

        blackProgram = new ShaderProgram(baseVs, new Shader(mContextWeakReference.get(), "blackFs.glsl", GLES20.GL_FRAGMENT_SHADER));
        blackProgram.link();

        Shader slitFs = new Shader(mContextWeakReference.get(), "mixedTextureFs.glsl", GLES20.GL_FRAGMENT_SHADER);
        slitScanProgram = new ShaderProgram(baseVs, slitFs);
        slitScanProgram.link();

        //create a plane-renderer to do the drawing
        mPlaneRenderer = new PlaneRenderer();

        //create the FBOS
        fbos = new FrameBuffer[MAX_SLITS];
        mTextureWidth = (int)(((float)mSurfaceWidth)* mTextureScale);
        mTextureHeight = (int)(((float)mSurfaceHeight) * mTextureScale);


        for( int i=0; i<MAX_SLITS; i++ ){
            fbos[i] = new FrameBuffer(mTextureWidth, mTextureHeight);
        }

        mSamplerBufferHigh = new FrameBuffer(mTextureWidth, mTextureHeight);
        mSamplerBufferLow = new FrameBuffer(mTextureWidth/2, mTextureHeight/2);

        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);

        //paint all fbos black once
        for( int i=0; i<fbos.length; i++ )
        {
            FrameBuffer fbo = fbos[i];
            //which one this is doesn't matter
            FrameBuffer other = fbos[(i+1)%fbos.length];
            fbo.bind();
            //the `other` texture doesn't matter its just that black program expects a texture uniform that it wont use
            mPlaneRenderer.predraw(blackProgram.getProgramId());
            mPlaneRenderer.draw1(blackProgram.getProgramId(), other.getTexture());
        }
    }


    /**
     * cycle all of the fbo buffers down an index.
     * The first buffer becomes the last.
     */
    public void cycleBuffers(){
        FrameBuffer first = fbos[0];

        for( int i=0; i<fbos.length -1; i++ ){
            fbos[i] = fbos[i+1];
        }

        fbos[fbos.length-1] = first;
    }


    @Override
    public void onDrawFrame(){
        super.onDrawFrame();
        mFrameCount++;

        GLES20.glUseProgram(mCameraShaderProgram);

        if( mFrameCount % getNumFramesBetweenUpdate() == 0 )
        {
            cycleBuffers();
        }

        fbos[fbos.length-1].bind();

        setUniformsAndAttribs();

        drawElements();

        if( mSampler != null )
        {
            if( !mSampler.isBuilt() )
            {
                mSampler.build();
            }
            mSampler.setBuffer(mPlaneRenderer);

            //provide a framebuffer at the resolution requested by DataSampler
            if(mSampler.getRequiredResolution() == DataSampler.Resolution.LOW)
            {
                mSampler.setFrameBuffer(mSamplerBufferLow);
            } else
            {
                mSampler.setFrameBuffer(mSamplerBufferHigh);
            }
            mSampler.draw();
        }

        if( mFBOTextures == null )
        {
            mFBOTextures = new int[fbos.length];
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        for( int i=0; i<fbos.length; i++ ){
            mFBOTextures[i] = fbos[i].getTexture();
        }

        if( mSampler != null )
        {
            mFBOTextures[0] = mSampler.getFrameBuffer().getTexture();
        }

        int programId =  slitScanProgram.getProgramId();
        mPlaneRenderer.predraw(programId);

        int uUseTexCoordLoc = GLES20.glGetUniformLocation(programId, "u_useTexCoord");
        GLES20.glUniform1i(uUseTexCoordLoc, mSampler.hasRepeatingRGChannels() ? 0 : 1);

        int uNumFrames = GLES20.glGetUniformLocation(programId, "u_numFrames");
        GLES20.glUniform1i(uNumFrames, mNumSlits);

        mPlaneRenderer.drawTextures(programId, mFBOTextures, 0);
    }
}
