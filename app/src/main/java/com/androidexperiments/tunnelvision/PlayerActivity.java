package com.androidexperiments.tunnelvision;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.VideoView;

import com.androidexperiments.tunnelvision.utils.AndroidUtils;

import java.io.File;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by kylephillips on 6/22/15.
 */
public class PlayerActivity extends FragmentActivity implements AndroidUtils.OnDeleteFilesCompleteListener
{

    public static final String EXTRA_VIDEO_PATH = "extra_video_path";

    protected static final String TAG = PlayerActivity.class.getSimpleName();


    protected File mFileToPlay;

    @InjectView(R.id.video_view)
    VideoView mVideoView;


    @Override
    protected void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_player);
        ButterKnife.inject(this);

        Bundle extras = getIntent().getExtras();

        if( extras != null )
        {
            mFileToPlay = new File(extras.getString(EXTRA_VIDEO_PATH));
        }


    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if( mFileToPlay == null || !mFileToPlay.exists() )
        {
            onBackPressed();
            return;
        }
        mVideoView.setVideoPath(mFileToPlay.getAbsolutePath());
        AndroidUtils.goFullscreen(this);

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

        mVideoView.start();
    }


    @OnClick(R.id.ok_button)
    public void onClickOk()
    {

        //Tell Android's Photos app to find this file
        MediaScannerConnection.scanFile(
                this,
                new String[]{mFileToPlay.getAbsolutePath()},
                null,
                new MediaScannerConnection.OnScanCompletedListener()
                {
                    @Override
                    public void onScanCompleted(String path, Uri uri)
                    {
                        Log.d(TAG, "file " + path + " was scanned successfully: " + uri);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                moveToShareActivity();
                            }
                        });
                    }
                }
        );

    }

    protected void moveToShareActivity()
    {
        Intent intent = new Intent(this, ShareActivity.class);
        intent.putExtra(ShareActivity.EXTRA_VIDEO_PATH, mFileToPlay.getAbsolutePath());
        //remove this from back-stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    protected void returnToMainActivity()
    {
        onBackPressed();
/*        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_VIDEO_ACCEPTED, true);
        startActivity(intent);*/
    }

    @OnClick(R.id.delete_button)
    public void onClickDelete()
    {
        Log.d(TAG, "SHOULD REMOVE FILE NOW");

        ArrayList<File> file = new ArrayList<>();
        file.add(mFileToPlay);

        AndroidUtils.FileDeleteTask deleteTask = new AndroidUtils.FileDeleteTask(this, file);
        deleteTask.execute();
    }

    @Override
    public void onDeleteFilesComplete(String[] filesToDelete)
    {
        returnToMainActivity();
    }
}
