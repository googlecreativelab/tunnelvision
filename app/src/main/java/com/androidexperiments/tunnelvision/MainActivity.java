package com.androidexperiments.tunnelvision;

import android.Manifest;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidexperiments.shadercam.fragments.CameraFragment;
import com.androidexperiments.shadercam.fragments.PermissionsHelper;
import com.androidexperiments.shadercam.gl.CameraRenderer;
import com.androidexperiments.tunnelvision.datatextures.DataSamplerAdapter;
import com.androidexperiments.tunnelvision.gl.SlitScanRenderer;
import com.androidexperiments.tunnelvision.utils.AndroidUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;


public class MainActivity extends FragmentActivity implements CameraRenderer.OnRendererReadyListener,
        PermissionsHelper.PermissionsListener
{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG_CAMERA_FRAGMENT = "tag_cam_frag";

    @Bind(R.id.texture)
    TextureView mTextureView;

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
    private CameraFragment mCameraFragment;

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
    private boolean mPermissionsSatisfied = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setupCameraFragment(CameraFragment.CAMERA_FORWARD);

        //attach GUI events
        mDelaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                //its possible for the seekbar to change value when a renderer has not yet been constructed
                if (mRenderer != null)
                {
                    //inverse value to seekbar progress
                    mRenderer.setNumFramesBetweenUpdate(seekBar.getMax() + 1 - progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        mTextureView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                onTouchEvent(event);
                //pass touch into data-samplers for them to use as wanted
                if (mRenderer.getSampler() != null)
                {
                    mRenderer.getSampler().onTouch(v, event);
                }
                return true;
            }
        });

        mCameraToggleButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                int nextCamera = (mCurrentCameraToUse == CameraFragment.CAMERA_FORWARD) ?
                        CameraFragment.CAMERA_PRIMARY : CameraFragment.CAMERA_FORWARD;

                setupCameraFragment(nextCamera);
            }
        });

        //setup permissions for M or start normally
        if(PermissionsHelper.isMorHigher())
            setupPermissions();

    }

    //the click is on the layout so its not such a difficult target
    @OnClick(R.id.filterLayout)
    protected void onClickGUIToggle()
    {
        int nextVisibility = mHorizontalScrollView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
        mCollapseButton.setBackgroundResource(nextVisibility == View.VISIBLE ? R.drawable.collapse_arrow : R.drawable.expand_arrow);
        mHorizontalScrollView.setVisibility(nextVisibility);
    }



    private void setupPermissions() {
        mPermissionsHelper = PermissionsHelper.attach(this);
        mPermissionsHelper.setRequestedPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE

        );
    }


    /**
     * move to the activity where we show the recorded video
     */
    protected void showRecordedVideo()
    {
        String absPath = mCurrentVideoFile.getAbsolutePath();
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_PATH, absPath);
        startActivity(intent);
        mCurrentVideoFile = null;
    }

    private void startRecording()
    {
        mCurrentVideoFile = getVideoFile();

        //in a few milliseconds re-enable the record button to avoid button-mashing
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mRecordButton.setEnabled(true);
            }
        }, 500);

        //update the counter of how long recording has been active
        mTimeElapsedTextView.setText("0:00");
        mTimeElapsedHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mSecondsElapsed++;
                int minutes = (int) Math.floor(mSecondsElapsed / 60);
                int seconds = mSecondsElapsed % 60;

                String time = Integer.toString(minutes) + ":" + paddedString(seconds);

                mTimeElapsedTextView.setText(time);
                if(mRenderer != null && mRenderer.isRecording())
                {
                    mTimeElapsedHandler.postDelayed(this, 1000);
                } else {
                    mTimeElapsedTextView.setText("");
                    mSecondsElapsed = 0;
                }
            }
        }, 1000);

        if(!mRenderer.isRecording())
        {
            mRenderer.startRecording(mCurrentVideoFile);
        }
        mRecordButton.setBackgroundResource(R.drawable.record_on);
    }

    private void stopRecording()
    {
        if(mRenderer.isRecording())
        {
            mRenderer.stopRecording();
        }
        mRecordButton.setBackgroundResource(R.drawable.record_off);

        //restart the camera with anew surface
        shutdownCamera();

        Toast.makeText(this, "File recording complete: " + getVideoFile().getAbsolutePath(), Toast.LENGTH_LONG).show();
    }


    private void setupCameraFragment(int cameraToUse)
    {
        if (cameraToUse != CameraFragment.CAMERA_FORWARD && cameraToUse != CameraFragment.CAMERA_PRIMARY)
        {
            Log.e(TAG, "INVALID CAMERA SELECTED, using primary instead");
            cameraToUse = CameraFragment.CAMERA_PRIMARY;
        }

        mCurrentCameraToUse = cameraToUse;

        if( mCameraFragment != null )
        {
            mCameraFragment.onPause();
            mCameraFragment.closeCamera();
            mCameraFragment.setCameraToUse(cameraToUse);
            mCameraFragment.onResume();
            mCameraFragment.openCamera();
            return;
        }

        mCameraFragment = CameraFragment.getInstance();
        mCameraFragment.setCameraToUse(cameraToUse);
        mCameraFragment.setTextureView(mTextureView);
        //mCameraFragment.setSurfaceTextureListener(mCameraTextureListener);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(mCameraFragment, TAG_CAMERA_FRAGMENT);
        transaction.commit();

    }

    /**
     * {@link ButterKnife} uses annotations to make setting {@link android.view.View.OnClickListener}'s
     * easier than ever with the {@link OnClick} annotation.
     */
    @OnClick(R.id.recordToggleButton)
    public void onClickRecord()
    {
        //avoid errors from people rapidly tapping the button
        mRecordButton.setEnabled(false);

        if(mRenderer.isRecording())
            stopRecording();
        else
            startRecording();
    }

    @OnClick(R.id.filterTunnelRepeat)
    public void onClickTunnelRepeat()
    {
        mRenderer.setSampler(mDataSamplerAdapter.get(DataSamplerAdapter.Sampler.TUNNEL_REPEAT));
    }

    @OnClick(R.id.filterTunnel)
    public void onClickTunnel()
    {
        mRenderer.setSampler(mDataSamplerAdapter.get(DataSamplerAdapter.Sampler.TUNNEL));
    }

    @OnClick(R.id.filterTwirl)
    public void onClickTwirl()
    {
        mRenderer.setSampler(mDataSamplerAdapter.get(DataSamplerAdapter.Sampler.TWIRL));
    }

    @OnClick(R.id.filterNoise)
    public void onClickNoise()
    {
        mRenderer.setSampler(mDataSamplerAdapter.get(DataSamplerAdapter.Sampler.NOISE));
    }

    @OnClick(R.id.filterNoiseBitmap)
    public void onClickNoiseBitmap()
    {
        mRenderer.setSampler(mDataSamplerAdapter.get(DataSamplerAdapter.Sampler.NOISE_BITMAP));
    }

    @OnClick(R.id.filterVertical)
    public void onClickVertical()
    {
        mRenderer.setSampler(mDataSamplerAdapter.get(DataSamplerAdapter.Sampler.VERTICAL));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mIsPaused = false;
        AndroidUtils.goFullscreen(this);
        if(PermissionsHelper.isMorHigher() && !mPermissionsSatisfied) {
            if(!mPermissionsHelper.checkPermissions())
                return;
            else
                mPermissionsSatisfied = true; //extra helper as callback sometimes isnt quick enough for future results
        }

        //if the recording was interrupted by pausing,
        //show the PlayerActivity now
        if(mShowPlayerOnResume){
            mShowPlayerOnResume = false;
            showRecordedVideo();
        }

        mDelaySeekBar.setProgress(mDelaySeekBar.getMax());
        mRadioGroup.check(R.id.filterTunnelRepeat);
        mRecordButton.setEnabled(true);

        if(!mTextureView.isAvailable())
            mTextureView.setSurfaceTextureListener(mCameraTextureListener); //set listener to handle when its ready
        else
            setReady(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());

    }

    private void shutdownCamera()
    {
        //protect from instances
        if( mRenderer != null )
        {
            CameraRenderer.RenderHandler handler = mRenderer.getRenderHandler();
            mRenderer = null;
            mCameraFragment.closeCamera();
            if(handler != null)
            {
                handler.sendShutdown();
            }
        }
    }

    @Override
    protected void onPause()
    {
        mIsPaused = true;

        if(mRenderer != null && mRenderer.isRecording())
        {
            mRenderer.stopRecording();
        }

        mRecordButton.setBackgroundResource(R.drawable.record_off);
        shutdownCamera();
        mTextureView.setSurfaceTextureListener(null);

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private String paddedString(int val)
    {
        return val < 10 ? "0"+val : String.valueOf(val);
    }

    private File getVideoFile()
    {
        //ensure the directory exists
        String dir = Environment.getExternalStorageDirectory() + File.separator + "TunnelVision" + File.separator;
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
        String filePrefix = "tunnelvision-" + month + paddedString(day) + "-" + (paddedString(hour) + "" + paddedString(minute));

        String fileName = filePrefix;
        String ext = ".mp4";

        File file = new File(root, fileName + ext);

        int i = 1;
        //If multiple videos are recorded in the same minute: tunnelvision-Jun24-1918-#.mp4
        while( file.exists() ){
            fileName = filePrefix + "-"+i;
            file = new File(root, fileName + ext);
            i++;
        }

        return file;
    }

    private void setReady(SurfaceTexture surface, int width, int height)
    {
        //create renderer and wait for preview texture creation
        mRenderer = new SlitScanRenderer(this, surface, mCameraFragment, width, height, 16);
        mRenderer.setOnRendererReadyListener(this);

        mDataSamplerAdapter = new DataSamplerAdapter(getApplicationContext(), width, height);
        if (mPermissionsSatisfied) {
            mRenderer.setSampler(mDataSamplerAdapter.next());
            mRenderer.start();
            mCameraFragment.configureTransform(width, height);
        } else {
            if(PermissionsHelper.isMorHigher())
                setupPermissions();
        }
    }


    @Override
    public void onRendererReady() {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if( mRenderer != null )
                {
                    mCameraFragment.setPreviewTexture(mRenderer.getPreviewTexture());
                    mCameraFragment.openCamera();

                    Log.d(TAG, "openCamera() called. videoSize: " + mCameraFragment.getVideoSize());
                }
            }
        });
    }

    @Override
    public void onRendererFinished()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (mCurrentVideoFile != null)
                {
                    //this could get called while in a paused state
                    //don't show recorded video unless we are currently active
                    if (mIsPaused)
                    {
                        mShowPlayerOnResume = true;
                        return;
                    }
                    showRecordedVideo();
                }
            }
        });
    }


    /**
     * waits for the textureview to be good to go
     */
    private TextureView.SurfaceTextureListener mCameraTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
        {
            setReady(surface, width, height);

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height)
        {
            mCameraFragment.configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    public void onPermissionsSatisfied() {
        mPermissionsSatisfied = true;
    }

    @Override
    public void onPermissionsFailed(String[] strings) {
        Log.e(TAG, "onPermissionsFailed()" + Arrays.toString(strings));
        mPermissionsSatisfied = false;
        Toast.makeText(this, "shadercam needs all permissions to function, please try again.", Toast.LENGTH_LONG).show();
        this.finish();
    }
}
