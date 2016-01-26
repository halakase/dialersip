package com.ammatti.stanley.sipdialer.dialer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.ammatti.stanley.sipdialer.R;
import com.ammatti.stanley.sipdialer.SipApplication;
import com.ammatti.stanley.sipdialer.SipService;
import com.ammatti.stanley.sipdialer.events.Event;
import com.ammatti.stanley.sipdialer.events.EventName;
import com.ammatti.stanley.sipdialer.events.others.AndroidVideoWindowImplEvent;
import com.ammatti.stanley.sipdialer.events.others.SurfaceViewEvent;
import com.ammatti.stanley.sipdialer.events.requests.AcceptCallRequest;
import com.ammatti.stanley.sipdialer.events.responses.CallAcceptedResponse;
import com.ammatti.stanley.sipdialer.events.responses.CallDeclinedResponse;
import com.ammatti.stanley.sipdialer.events.responses.CallStoppedResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.MicOffResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.MicOnResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.RecOffResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.RecOnResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.SpeakerOffResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.SpeakerOnResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;
import org.linphone.mediastream.Log;
import org.linphone.mediastream.video.AndroidVideoWindowImpl;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;

/**
 * Created by 1310042 on 2016/1/22.
 */

public class InComingVideoCallActivity extends Activity {

    private static final String TAG = "InComingVideoCallActivity";

    private Bus aTos_bus;
    private Bus sToa_bus;
    private static SurfaceView mVideoView;
    private static SurfaceView mCaptureView;
    private AndroidVideoWindowImpl androidVideoWindowImpl;
    private PowerManager pm;
    private PowerManager.WakeLock wl;
    private float mZoomFactor = 1.f;
    private float mZoomCenterX, mZoomCenterY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videocall);

        mVideoView = (SurfaceView) findViewById(R.id.video_surface);
        mCaptureView = (SurfaceView) findViewById(R.id.video_capture_surface);
        mCaptureView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // Warning useless because value is ignored and automatically set by new APIs.
    }

    @Override
    public void onResume() {
        super.onResume();
        aTos_bus = SipApplication.getActivityToServiceBusInstance();
        sToa_bus = SipApplication.getServiceToActivityBusInstance();
        sToa_bus.register(this);
        //accept call by default
        aTos_bus.post(new AcceptCallRequest(EventName.ACCEPT_CALL_REQUEST));
        //fixZOrder(mVideoView, mCaptureView);

        androidVideoWindowImpl = new AndroidVideoWindowImpl(mVideoView, mCaptureView);
        androidVideoWindowImpl.init();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                //prepare to execute video task
                setVideo();
            }
        }, 2000);
    }

    @Override
    public void onPause() {
        super.onPause();
        sToa_bus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setVideo() {

        //setPreviewWindow
        SurfaceViewEvent event = new SurfaceViewEvent(EventName.SURFACEVIEW_EVENT);
        event.setView(mCaptureView);
        aTos_bus.post(event);
        //setVideoWindow
        AndroidVideoWindowImplEvent event2 = new AndroidVideoWindowImplEvent(EventName.ANDROIDVIDEOWIN_EVENT);
        event2.setWindow(androidVideoWindowImpl);
        aTos_bus.post(event2);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //MyTag可以随便写,可以写应用名称等
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MyTag");
        //在释放之前，屏幕一直亮着（有可能会变暗,但是还可以看到屏幕内容,换成PowerManager.SCREEN_BRIGHT_WAKE_LOCK不会变暗）
        wl.acquire();
    }

    private void fixZOrder(SurfaceView video, SurfaceView preview) {
        video.setZOrderOnTop(false);
        preview.setZOrderOnTop(true);
        preview.setZOrderMediaOverlay(true); // Needed to be able to display control layout over
    }

    @Subscribe
    public void onEvent(Event event) {
        if (event instanceof CallAcceptedResponse) {
            //startTimer();
            //set up audio
        } else if (event instanceof CallStoppedResponse) {
            Log.i(TAG, "InComingVideoCallActivity got CallStoppedResponse");
            //stopTimer();
            //release audio
            InComingVideoCallActivity.this.finish();
        } else if (event instanceof CallDeclinedResponse) {
            Log.i(TAG, "InComingVideoCallActivity got CallDeclinedResponse");
            //stopTimer();
            //release audio
        } else if (event instanceof MicOffResponse) {
            //MIC_TURN_OFF_VIEW.setVisibility(View.GONE);
            //MIC_TURN_ON_VIEW.setVisibility(View.VISIBLE);
        } else if (event instanceof MicOnResponse) {
            //MIC_TURN_OFF_VIEW.setVisibility(View.VISIBLE);
            //MIC_TURN_ON_VIEW.setVisibility(View.GONE);
        } else if (event instanceof RecOffResponse) {
            //REC_START_VIEW.setVisibility(View.VISIBLE);
        } else if (event instanceof RecOnResponse) {
            //REC_START_VIEW.setVisibility(View.GONE);
        } else if (event instanceof SpeakerOffResponse) {
            //SPEAKER_ON_VIEW.setVisibility(View.VISIBLE);
            //SPEAKER_OFF_VIEW.setVisibility(View.GONE);
        } else if (event instanceof SpeakerOnResponse) {
            //SPEAKER_ON_VIEW.setVisibility(View.GONE);
            //SPEAKER_OFF_VIEW.setVisibility(View.VISIBLE);
        } else {
            Log.i(TAG, "InComingVideoCallActivity got undefind Event");
        }
    }
}
