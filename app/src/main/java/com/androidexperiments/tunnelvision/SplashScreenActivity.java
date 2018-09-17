package com.androidexperiments.tunnelvision;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;

import com.androidexperiments.tunnelvision.utils.AndroidUtils;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by kylephillips on 7/14/15.
 */
public class SplashScreenActivity extends FragmentActivity implements MediaPlayer.OnCompletionListener
{

    protected static final String TAG = SplashScreenActivity.class.getSimpleName();

    @Bind(R.id.splashvideo)
    VideoView mSplashVideo;


    protected boolean mIsExiting = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        ButterKnife.bind(this);

        mSplashVideo.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if( event.getAction() == MotionEvent.ACTION_DOWN )
                {
                    moveToMainActivity();
                }
                return true;
            }
        });

    }

    @OnClick(R.id.infoButton)
    protected void onInfoClicked()
    {
        Intent intent = new Intent(this, OpenSourceLicenseActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        AndroidUtils.goFullscreen(this);

        int[] assets = {
                R.raw.covers_flower,
                R.raw.cover_facade_lo,
                R.raw.cover_waves_lo
        };

        int asset = assets[ (int)(Math.floor(Math.random()*assets.length)) ];
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + asset);

        mSplashVideo.setOnCompletionListener(this);
        mSplashVideo.setVideoURI(uri);
        mSplashVideo.start();
    }

    @Override
    public void onPause()
    {
        mSplashVideo.stopPlayback();
        super.onPause();
    }

    protected void moveToMainActivity()
    {
        if( mIsExiting ){
            //make sure this doesnt happen twice
            return;
        }
        mIsExiting = true;
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public void onCompletion(MediaPlayer mp)
    {
        moveToMainActivity();
    }
}
