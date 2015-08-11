package com.androidexperiments.tunnelvision.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RadioButton;

import com.androidexperiments.tunnelvision.R;


/**
 * Created by kylephillips on 6/17/15.
 */
public class FilterRadioButton extends RadioButton
{


    Drawable mIcon;


    public FilterRadioButton(Context context) {
        super(context, null);
    }

    public FilterRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs, R.style.CustomRadio);
        init(attrs);
    }

    public FilterRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
        init(attrs);
    }

    public FilterRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs)
    {

        int imageId = -1;
        String id = getResources().getResourceName(getId());
        id = id.substring(id.indexOf("id/") + 3);
        if( id != null )
        {
            switch (id)
            {
                case "filterTunnelRepeat":
                    imageId = R.drawable.tunnel_repeat_shader;
                    break;

                case "filterTunnel":
                    imageId = R.drawable.tunnel_shader;
                    break;

                case "filterTwirl":
                    imageId = R.drawable.twirl_shader;
                    break;

                case "filterNoiseBitmap":
                    imageId = R.drawable.noise_bitmap_shader;
                    break;

                case "filterNoise":
                    imageId = R.drawable.noise_shader;
                    break;

                case "filterVertical":
                    imageId = R.drawable.vertical_shader;
                    break;

                default:
                    imageId = R.drawable.vertical_shader;
            }
            mIcon = getResources().getDrawable(imageId, null);
        }

    }


    @Override
    public void onDraw(Canvas canvas)
    {
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        super.onDraw(canvas);

        if( mIcon != null )
        {
            mIcon.setBounds(0, 0, w, h);
            mIcon.draw(canvas);
        }

        Paint paint = new Paint();
        int color = isChecked() ? R.color.selected : R.color.unselected;
        paint.setColor(getResources().getColor(color));

        //device pixels yay
        float sw = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

        paint.setStrokeWidth(sw);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(0, 0, w, h, paint);
    }
}
