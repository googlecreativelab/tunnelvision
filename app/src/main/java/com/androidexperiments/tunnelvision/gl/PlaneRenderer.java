package com.androidexperiments.tunnelvision.gl;

import android.opengl.GLES20;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by kylephillips on 5/5/15.
 */
public class PlaneRenderer
{

    public static final int BYTES_PER_FLOAT = 4;

    protected FloatBuffer vertexBufferData;
    protected FloatBuffer texBufferData;


    public PlaneRenderer(){

        //VERTICES
        float sz = 1f;
        float[] verts = new float[]{
                -sz, sz,  0.0f,
                -sz, -sz, 0.0f,
                sz, sz, 0.0f,
                sz, -sz, 0.0f
        };

        vertexBufferData = fillBuffer(verts);


        //TEXTURE
        float[] texCoords = new float[]{
                0f, 1f,
                0f, 0f,
                1f, 1f,
                1f, 0f
        };

        texBufferData = fillBuffer(texCoords);

    }

    /**
     * Convert an array of floats into a FloatBuffer for GLES20
     * @param arr
     * @return
     */
    protected FloatBuffer fillBuffer(float[] arr){
        // Allocate a direct block of memory on the native heap,
        // size in bytes is equal to cubePositions.length * BYTES_PER_FLOAT.
        // BYTES_PER_FLOAT is equal to 4, since a float is 32-bits, or 4 bytes.
        FloatBuffer buffer = ByteBuffer.allocateDirect(arr.length * BYTES_PER_FLOAT)
                // Floats can be in big-endian or little-endian order.
                // We want the same as the native platform.
                .order(ByteOrder.nativeOrder())
                        // Give us a floating-point view on this byte buffer.
                .asFloatBuffer();

        //copy the data over and reset the buffers position
        buffer.put(arr)
                .position(0);

        return buffer;
    }



    public void predraw( int program ){
        GLES20.glUseProgram(program);

        int vertsAttrib = GLES20.glGetAttribLocation(program, "position");
        int texAttrib = GLES20.glGetAttribLocation(program, "texCoord");


        if(vertsAttrib >= 0)
        {
            GLES20.glEnableVertexAttribArray(vertsAttrib);
            GLES20.glVertexAttribPointer(vertsAttrib, 3, GLES20.GL_FLOAT, false, 0, vertexBufferData);
        }


        if(texAttrib >= 0)
        {
            GLES20.glEnableVertexAttribArray(texAttrib);
            GLES20.glVertexAttribPointer(texAttrib, 2, GLES20.GL_FLOAT, false, 0, texBufferData);
        }
    }



    public void draw1( int program, int tex1 ){

        int uImageLoc = GLES20.glGetUniformLocation(program, "u_image1");

        GLES20.glUniform1i(uImageLoc, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex1);

        drawArrays();

    }

    public void drawArrays()
    {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }


    public void draw2( int program, int tex1, int tex2 ){

        int uImage1Loc = GLES20.glGetUniformLocation(program, "u_image1");
        int uImage2Loc = GLES20.glGetUniformLocation(program, "u_image2");

        GLES20.glUniform1i(uImage1Loc, 0);
        GLES20.glUniform1i(uImage2Loc, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex2);

        drawArrays();
    }


    public void drawTextures( int program, int[] texIds, int offset ){

        for( int i=0; i<texIds.length; i++ ){
            int uLoc = GLES20.glGetUniformLocation(program, "u_image"+(i+1+offset));
            GLES20.glUniform1i(uLoc, i);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIds[i]);
        }

        drawArrays();
    }


}
