package com.androidexperiments.tunnelvision;

import com.androidexperiments.shadercam.fragments.PermissionsHelper;
import com.androidexperiments.shadercam.fragments.VideoFragment;
import com.androidexperiments.shadercam.gl.VideoRenderer;
import com.androidexperiments.tunnelvision.datatextures.DataSamplerAdapter;
import com.androidexperiments.tunnelvision.gl.SlitScanRenderer;
import com.androidexperiments.tunnelvision.utils.AndroidUtils;
import com.uncorkedstudios.android.view.recordablesurfaceview.RecordableSurfaceView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TAG_CAMERA_FRAGMENT = "tag_cam_frag";

    @Bind(R.id.texture)
    RecordableSurfaceView mRecordableSurfaceView;

    @Bind(R.id.delaySeekBar)
    SeekBar mDelaySeekBar;

    @Bind(R.id.collapseButton)
    ImageButton mCollapseButton;

    @Bind(R.id.cameraToggleButton)
    ImageButton mCameraToggleButton;

    @Bind(R.id.recordToggleButton)
    ImageButton mRecordButton;

    @Bind(R.id.timeElapsedTextView)
    TextView mTimeElapsedTextView;

    @Bind(R.id.horizontalScrollView)
    HorizontalScrollView mHorizontalScrollView;

    @Bind(R.id.filterRadioGroup)
    RadioGroup mRadioGroup;

    private File mCurrentVideoFile;

    //Camera
    private int mCurrentCameraToUse;

    private VideoFragment mVideoFragment;

    //renderers
    private SlitScanRenderer mRenderer;

    private DataSamplerAdapter mDataSamplerAdapter;

    //is the activity in a paused state?
    private boolean mIsPaused = false;

    //should we go to PlayerActivity now that we resumed?
    private boolean mShowPlayerOnResume = false;

    private int mSecondsElapsed = 0;

    private Handler mTimeElapsedHandler = new Handler();

    private PermissionsHelper mPermissionsHelper;


    private boolean mIsRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        //attach GUI events
        mDelaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //its possible for the seekbar to change value when a renderer has not yet been constructed
                if (mRenderer != null) {
                    //inverse value to seekbar progress
                    mRenderer.setNumFramesBetweenUpdate(seekBar.getMax() + 1 - progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mRecordableSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEvent(event);
                //pass touch into data-samplers for them to use as wanted
                if (mRenderer.getSampler() != null) {
                    mRenderer.getSampler().onTouch(v, event);
                }
                return true;
            }
        });

        mCameraToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mVideoFragment.swapCamera();
            }
        });


    }

    //the click is on the layout so its not such a difficult target
    @OnClick(R.id.filterLayout)
    protected void onClickGUIToggle() {
        int nextVisibility = mHorizontalScrollView.getVisibility() == View.VISIBLE ? View.GONE
                : View.VISIBLE;
        mCollapseButton.setBackgroundResource(
                nextVisibility == View.VISIBLE ? R.drawable.collapse_arrow
                        : R.drawable.expand_arrow);
        mHorizontalScrollView.setVisibility(nextVisibility);
    }


    /**
     * move to the activity where we show the recorded video
     */
    protected void showRecordedVideo() {
        String absPath = mCurrentVideoFile.getAbsolutePath();
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_PATH, absPath);
        startActivity(intent);
        mCurrentVideoFile = null;
    }

    private void startRecording() {
        mCurrentVideoFile = getVideoFile();

        //in a few milliseconds re-enable the record button to avoid button-mashing
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecordButton.setEnabled(true);
            }
        }, 500);

        //update the counter of how long recording has been active
        mTimeElapsedTextView.setText("0:00");
        mTimeElapsedHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSecondsElapsed++;
                int minutes = (int) Math.floor(mSecondsElapsed / 60);
                int seconds = mSecondsElapsed % 60;

                String time = Integer.toString(minutes) + ":" + paddedString(seconds);

                mTimeElapsedTextView.setText(time);
                if (mRenderer != null && mIsRecording) {
                    mTimeElapsedHandler.postDelayed(this, 1000);
                } else {
                    mTimeElapsedTextView.setText("");
                    mSecondsElapsed = 0;
                }
            }
        }, 1000);

        if (!mIsRecording) {
            mIsRecording = true;
            mRecordableSurfaceView.startRecording();
        }
        mRecordButton.setBackgroundResource(R.drawable.record_on);
    }

    private void stopRecording() {
        if (mIsRecording) {

            mTimeElapsedTextView.setText("0:00");
            mRecordableSurfaceView.stopRecording();
            try {
                mCurrentVideoFile = getVideoFile();
                android.graphics.Point size = new android.graphics.Point();
                getWindowManager().getDefaultDisplay().getRealSize(size);
                mRecordableSurfaceView.initRecorder(mCurrentVideoFile, size.x, size.y, null, null);
            } catch (IOException ioex) {
                Log.e(TAG, "Couldn't re-init recording", ioex);
            }

            mIsRecording = false;
        }
        mRecordButton.setBackgroundResource(R.drawable.record_off);
        mRecordButton.setEnabled(true);

        //restart the camera with anew surface

        Toast.makeText(this, "File recording complete: " + getVideoFile().getAbsolutePath(),
                Toast.LENGTH_LONG).show();
    }


    private void setupVideoFragment(int cameraToUse, VideoRenderer renderer) {
        if (cameraToUse != VideoFragment.CAMERA_FORWARD
                && cameraToUse != VideoFragment.CAMERA_PRIMARY) {
            Log.e(TAG, "INVALID CAMERA SELECTED, using primary instead");
            cameraToUse = VideoFragment.CAMERA_PRIMARY;
        }

        mCurrentCameraToUse = cameraToUse;

        mVideoFragment = VideoFragment.getInstance();
        mVideoFragment.setRecordableSurfaceView(mRecordableSurfaceView);
        mVideoFragment.setVideoRenderer(renderer);
        mVideoFragment.setCameraToUse(mCurrentCameraToUse);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(mVideoFragment, TAG_CAMERA_FRAGMENT);
        transaction.commit();
    }

    /**
     * {@link ButterKnife} uses annotations to make setting {@link android.view.View.OnClickListener}'s
     * easier than ever with the {@link OnClick} annotation.
     */
    @OnClick(R.id.recordToggleButton)
    public void onClickRecord() {
        //avoid errors from people rapidly tapping the button
        mRecordButton.setEnabled(false);

        if (mIsRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    @OnClick(R.id.filterTunnelRepeat)
    public void onClickTunnelRepeat() {
        mRenderer.setSampler(mDataSamplerAdapter.get(DataSamplerAdapter.Sampler.TUNNEL_REPEAT));
    }

    @OnClick(R.id.filterTunnel)
    public void onClickTunnel() {
        mRenderer.setSampler(mDataSamplerAdapter.get(DataSamplerAdapter.Sampler.TUNNEL));
    }

    @OnClick(R.id.filterTwirl)
    public void onClickTwirl() {
        mRenderer.setSampler(mDataSamplerAdapter.get(DataSamplerAdapter.Sampler.TWIRL));
    }

    @OnClick(R.id.filterNoise)
    public void onClickNoise() {
        mRenderer.setSampler(mDataSamplerAdapter.get(DataSamplerAdapter.Sampler.NOISE));
    }

    @OnClick(R.id.filterNoiseBitmap)
    public void onClickNoiseBitmap() {
        mRenderer.setSampler(mDataSamplerAdapter.get(DataSamplerAdapter.Sampler.NOISE_BITMAP));
    }

    @OnClick(R.id.filterVertical)
    public void onClickVertical() {
        mRenderer.setSampler(mDataSamplerAdapter.get(DataSamplerAdapter.Sampler.VERTICAL));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PermissionsHelper.isMorHigher()) {
            String[] perms = new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};

            boolean permCheck = true;
            for (String perm : perms) {
                if (this.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                    permCheck = false;
                }
            }
            //a little wonky here, but given the risk of slamming into a fatal camera permissions
            //error here, we kick it back to the splash screen to ensure that there aren't orphaned
            //GL artifacts hanging around.
            if (!permCheck) {
                Intent i = new Intent(this, SplashScreenActivity.class);
                startActivity(i);

            }
        }

        mIsPaused = false;
        AndroidUtils.goFullscreen(this);

        //if the recording was interrupted by pausing,
        //show the PlayerActivity now
        if (mShowPlayerOnResume) {
            mShowPlayerOnResume = false;
            showRecordedVideo();
        }

        mDelaySeekBar.setProgress(mDelaySeekBar.getMax());
        mRadioGroup.check(R.id.filterTunnelRepeat);
        mRecordButton.setEnabled(true);

        //create renderer and wait for preview texture creation
        final android.graphics.Point size = new android.graphics.Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);

        mRenderer = new SlitScanRenderer(this,16);

        setupVideoFragment(VideoFragment.CAMERA_FORWARD, mRenderer);
        mDataSamplerAdapter = new DataSamplerAdapter(getApplicationContext(), size.x, size.y);
        mRenderer.setSampler(mDataSamplerAdapter.next());
        mRecordableSurfaceView.resume();

        try {
            mCurrentVideoFile = getVideoFile();
            mRecordableSurfaceView.initRecorder(mCurrentVideoFile, size.x, size.y, null, null);

        } catch (IOException ioex) {
            Log.e(TAG, "Couldn't re-init recording", ioex);
        }

    }


    private void shutdownCamera() {
        mRenderer = null;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(mVideoFragment);
        ft.commit();
        mVideoFragment = null;
    }


    @Override
    protected void onPause() {
        mIsPaused = true;

        if (mRenderer != null && mIsRecording) {
            mRecordableSurfaceView.stopRecording();
        }

        mRecordableSurfaceView.pause();

        mRecordButton.setBackgroundResource(R.drawable.record_off);
        shutdownCamera();

        AndroidUtils.cleanUpFileStubs();

        mIsRecording = false;

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private String paddedString(int val) {
        return val < 10 ? "0" + val : String.valueOf(val);
    }

    private File getVideoFile() {
        //ensure the directory exists
        String dir = Environment.getExternalStorageDirectory() + File.separator + "TunnelVision"
                + File.separator;
        File root = new File(dir);
        root.mkdirs();

        //format a timestamp as a human-readable string
        Calendar cal = GregorianCalendar.getInstance();
        String month = new SimpleDateFormat("MMM").format(cal.getTime());
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        //Preferred filename: tunnelvision-Jun24-1918.mp4
        //try this filename first, if its already taken, start adding a counter
        String filePrefix = "tunnelvision-" + month + paddedString(day) + "-" + (paddedString(hour)
                + "" + paddedString(minute));

        String fileName = filePrefix;
        String ext = ".mp4";

        File file = new File(root, fileName + ext);

        int i = 1;
        //If multiple videos are recorded in the same minute: tunnelvision-Jun24-1918-#.mp4
        while (file.exists()) {
            fileName = filePrefix + "-" + i;
            file = new File(root, fileName + ext);
            i++;
        }

        return file;
    }

}
