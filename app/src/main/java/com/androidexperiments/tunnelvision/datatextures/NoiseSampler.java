package com.androidexperiments.tunnelvision.datatextures;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.androidexperiments.tunnelvision.datatextures.canvas.CanvasSampler;

import java.util.Random;

/**
 * Created by kylephillips on 5/26/15.
 */
public class NoiseSampler extends CanvasSampler
{


    protected int getRandom()
    {
        Random rn = new Random();
        return rn.nextInt(getNumTones());
    }

    @Override
    public boolean draw()
    {
        if( !super.draw() )
        {
            return false;
        }


        Random rand = new Random();

        Canvas c = getCanvas();

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        for( int x=0; x<getWidth(); x++ )
        {

            for( int y=0; y<getHeight(); y++ )
            {
                int shade = rand.nextInt(mNumTones) + 1;

                paint.setColor(Color.argb(255, 0, 0, shade));

                c.drawPoint(x, y, paint);
            }
        }

        return true;
    }
}
