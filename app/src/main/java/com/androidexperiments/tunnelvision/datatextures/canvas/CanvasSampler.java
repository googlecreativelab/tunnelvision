package com.androidexperiments.tunnelvision.datatextures.canvas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

/**
 * Created by kylephillips on 5/26/15.
 */
public class CanvasSampler
{

    protected static final String TAG = CanvasSampler.class.getSimpleName();

    public int getWidth()
    {
        return mWidth;
    }

    protected int mWidth;

    public int getHeight()
    {
        return mHeight;
    }

    protected int mHeight;

    public Canvas getCanvas()
    {
        return mCanvas;
    }

    public void setCanvas(Canvas canvas)
    {
        mCanvas = canvas;
        mWidth = canvas.getWidth();
        mHeight = canvas.getHeight();
        getIntTones();
    }

    protected Canvas mCanvas;


    public int getNumTones()
    {
        return mNumTones;
    }

    public void setNumTones(int numTones)
    {
        mNumTones = numTones;
        getIntTones();
    }

    protected int mNumTones = 14;


    public CanvasSampler()
    {
    }

    public CanvasSampler(Canvas c)
    {
        setCanvas(c);
    }


    protected int[] mIntTones = new int[mNumTones];

    public int[] getIntTones()
    {
        return getIntTones( mIntTones );
    }

    public int[] getIntTones( int[] levels )
    {
        int mag = 255 / (mNumTones-1);

        for( int i=0; i<mNumTones; i++)
        {
            levels[i] = mag * i;
        }
        return levels;
    }




    public boolean draw()
    {
        if( getCanvas() == null ){
            Log.w(TAG, "Attempted to draw without a canvas set");
            return false;
        }

        return true;
    }



    public Bitmap toBitmap( int width, int height )
    {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        return toBitmap(b);
    }

    public Bitmap toBitmap( Bitmap bitmap )
    {
        Canvas c = new Canvas(bitmap);

        setCanvas(c);
        draw();

        return bitmap;
    }

}
