package com.androidexperiments.tunnelvision;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.widget.VideoView;

import com.androidexperiments.tunnelvision.utils.AndroidUtils;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by kylephillips on 7/17/15.
 */
public class ShareActivity extends FragmentActivity
{

    public static final String EXTRA_VIDEO_PATH = "extra_video_path";

    protected File mFileToShare;

    @InjectView(R.id.share_video_view)
    VideoView mVideoView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        ButterKnife.inject(this);


        Bundle extras = getIntent().getExtras();

        if( extras != null )
        {
            mFileToShare = new File(extras.getString(EXTRA_VIDEO_PATH));
        }

    }


    @Override
    protected void onResume()
    {
        super.onResume();

        if( mFileToShare == null || !mFileToShare.exists() )
        {
            onBackPressed();
            return;
        }

        AndroidUtils.goFullscreen(this);
        //mVideoView.setVideoPath(mFileToShare.getAbsolutePath());

        //when the video completes, wait 500 ms then replay it infinitely
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            protected Handler mDelayHandler = new Handler();

            @Override
            public void onCompletion(MediaPlayer mp)
            {
                mDelayHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mVideoView.start();
                    }
                }, 500);
            }
        });

        //mVideoView.start();
    }


    @OnClick(R.id.share_button)
    public void onClickShare()
    {
        Intent shareIntent = AndroidUtils.getShareIntent(mFileToShare, getResources().getString(R.string.share_description));
        startActivity(shareIntent);
    }

    @OnClick(R.id.new_button)
    public void onClickNew()
    {
        onBackPressed();
    }
}
