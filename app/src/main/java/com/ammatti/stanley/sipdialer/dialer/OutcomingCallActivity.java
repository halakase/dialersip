package com.ammatti.stanley.sipdialer.dialer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ammatti.stanley.sipdialer.R;
import com.ammatti.stanley.sipdialer.SipApplication;
import com.ammatti.stanley.sipdialer.events.EventName;
import com.ammatti.stanley.sipdialer.events.requests.StopCallRequest;
import com.ammatti.stanley.sipdialer.events.requests.dev.MicOffRequest;
import com.ammatti.stanley.sipdialer.events.requests.dev.MicOnRequest;
import com.ammatti.stanley.sipdialer.events.requests.dev.RecOffRequest;
import com.ammatti.stanley.sipdialer.events.requests.dev.RecOnRequest;
import com.ammatti.stanley.sipdialer.events.requests.dev.SpeakerOffRequest;
import com.ammatti.stanley.sipdialer.events.requests.dev.SpeakerOnRequest;
import com.ammatti.stanley.sipdialer.events.responses.CallStartedResponse;
import com.ammatti.stanley.sipdialer.events.responses.CallStoppedResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.MicOffResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.MicOnResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.RecOffResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.RecOnResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.SpeakerOffResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.SpeakerOnResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Created by user on 25.08.15.
 */
public class OutcomingCallActivity extends Activity {

    private static final String TAG = "OutcomingCallActivity";
    private Bus aTos_bus;
    private Bus sToa_bus;

    private LinearLayout ringerLayout;

    private int ACCEPT_INCOMING_VIEW_ID = R.id.accept_call;
    private int DENY_CALL_VIEW_ID = R.id.handoff_call;
    private int TIME_TEXT_VIEW_ID = R.id.time;
    private int MIC_TURN_OFF_ID = R.id.mic_off;
    private int MIC_TURN_ON_ID = R.id.mic_on;
    private int REC_START_ID = R.id.imRecStart;
    private int REC_STOP_ID = R.id.imgRecStop;
    private int SPEAKER_ON_ID = R.id.speaker_on;
    private int SPEAKER_OFF_ID = R.id.speaker_off;

    private ImageView MIC_TURN_OFF_VIEW;
    private ImageView MIC_TURN_ON_VIEW;
    private ImageView REC_START_VIEW;
    private ImageView REC_STOP_VIEW;
    private ImageView SPEAKER_ON_VIEW;
    private ImageView SPEAKER_OFF_VIEW;

    private RelativeLayout FOOTER;

    private TextView USER_NAME_VIEW;
    private static TextView TIMER_TEXT_VIEW;

    private Handler handler = new Handler();
    private boolean isTimerStarted;
    private boolean stopTimer;
    static int currentTimer = 0;

    Runnable updateTimer = new Runnable() {
        @Override
        public void run() {

            int seconds = currentTimer % 60;
            int minutes = currentTimer / 60;

            android.util.Log.e("TIMER", "currentTimer=" + currentTimer);
            android.util.Log.e("TIMER", "seconds=" + seconds);
            android.util.Log.e("TIMER", "minutes=" + minutes);

            if (TIMER_TEXT_VIEW != null)
                TIMER_TEXT_VIEW.setText(String.format("%02d:%02d", minutes, seconds));
            else
                android.util.Log.e("TIMER", "TIMER_TEXT_VIEW is NULL");

            if (!stopTimer) {
                handler.postDelayed(this, 1000);
                currentTimer++;
            }
        }
    };

    View.OnClickListener inCallListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == DENY_CALL_VIEW_ID) {
                // decline current call
                Log.i(TAG, "decline current call 1");
                if(isTimerStarted == true){
                    Log.i(TAG, "decline current call 2");
                    sToa_bus.unregister(OutcomingCallActivity.this);
                }
                aTos_bus.post(new StopCallRequest(EventName.STOP_CALL_REQUEST));
                Log.i(TAG, "decline current call 3");
                OutcomingCallActivity.this.finish();
                Log.i(TAG, "decline current call 4");
            } else if (view.getId() == MIC_TURN_OFF_ID) { // decline incoming
                sToa_bus.post(new MicOffRequest(EventName.MIC_OFF_REQUEST));

                /*not implemented*/
            } else if (view.getId() == MIC_TURN_ON_ID) { // decline incoming
                sToa_bus.post(new MicOnRequest(EventName.MIC_ON_REQUEST));

                /*not implemented*/
            } else if (view.getId() == REC_START_ID) { // decline incoming
                sToa_bus.post(new RecOnRequest(EventName.REC_ON_REQUEST));

                /*not implemented*/
            } else if (view.getId() == REC_STOP_ID) { // decline incoming
                sToa_bus.post(new RecOffRequest(EventName.REC_OFF_REQUEST));

                /*not implemented*/
            } else if (view.getId() == SPEAKER_ON_ID) { // decline incoming
                sToa_bus.post(new SpeakerOnRequest(EventName.SPEAKER_ON_REQUEST));

                /*not implemented*/
            } else if (view.getId() == SPEAKER_OFF_ID) { // decline incoming
                sToa_bus.post(new SpeakerOffRequest(EventName.SPEAKER_OFF_REQUEST));

                /*not implemented*/
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        // todo: SET REMOTE USER NAME
    }

    @Override
    protected void onResume() {
        super.onResume();
        aTos_bus = SipApplication.getActivityToServiceBusInstance();
        sToa_bus = SipApplication.getServiceToActivityBusInstance();
        sToa_bus.register(this);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        stopTimer();
        if(isTimerStarted == true){
            sToa_bus.unregister(this);
        }
    }

    public void setVideoWindow(Object w) {
        /*
        if (mVideoWindow != null) {
            mVideoWindow.setListener(null);
        }
        mVideoWindow = new AndroidVideoWindowImpl((SurfaceView) w);
        mVideoWindow.setListener(new AndroidVideoWindowImpl.VideoWindowListener() {
            public void onSurfaceDestroyed(AndroidVideoWindowImpl vw) {
                setVideoWindowId(nativePtr, null);
            }

            public void onSurfaceReady(AndroidVideoWindowImpl vw) {
                setVideoWindowId(nativePtr, vw);
            }
        });*/
    }

    private void initLayout() {
        setContentView(R.layout.outcomingcall_activity_layout);
        ringerLayout = (LinearLayout) findViewById(R.id.root_root);

        MIC_TURN_OFF_VIEW = (ImageView) ringerLayout.findViewById(MIC_TURN_OFF_ID);
        MIC_TURN_ON_VIEW = (ImageView) ringerLayout.findViewById(MIC_TURN_ON_ID);
        REC_STOP_VIEW = (ImageView) ringerLayout.findViewById(REC_STOP_ID);
        REC_START_VIEW = (ImageView) ringerLayout.findViewById(REC_START_ID);
        SPEAKER_ON_VIEW = (ImageView) ringerLayout.findViewById(SPEAKER_ON_ID);
        SPEAKER_OFF_VIEW = (ImageView) ringerLayout.findViewById(SPEAKER_OFF_ID);

        ringerLayout.findViewById(ACCEPT_INCOMING_VIEW_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(DENY_CALL_VIEW_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(MIC_TURN_OFF_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(MIC_TURN_ON_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(REC_STOP_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(REC_START_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(SPEAKER_ON_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(SPEAKER_OFF_ID).setOnClickListener(inCallListener);

        USER_NAME_VIEW = (TextView) ringerLayout.findViewById(R.id.callee_name);
        TIMER_TEXT_VIEW = (TextView) ringerLayout.findViewById(R.id.time);

        FOOTER = (RelativeLayout) ringerLayout.findViewById(R.id.footer_rel);
    }

    private void startTimer() {
        if (!isTimerStarted) {
            isTimerStarted = true;

            handler.post(updateTimer);
        }
    }

    private void stopTimer() {
        isTimerStarted = false;
        currentTimer = 0;
        stopTimer = true;
        handler.removeCallbacks(updateTimer);
    }

    @Subscribe
    public void onEvent(Object event) {
        if (event instanceof CallStartedResponse) {
            startTimer();
        } else if (event instanceof CallStoppedResponse) {
            Log.i(TAG, "OutcomingCallActivity got CallStoppedResponse");
            OutcomingCallActivity.this.finish();
        } else if (event instanceof MicOffResponse) {
            MIC_TURN_OFF_VIEW.setVisibility(View.GONE);
            MIC_TURN_ON_VIEW.setVisibility(View.VISIBLE);
        } else if (event instanceof MicOnResponse) {
            MIC_TURN_OFF_VIEW.setVisibility(View.VISIBLE);
            MIC_TURN_ON_VIEW.setVisibility(View.GONE);
        } else if (event instanceof RecOffResponse) {
            REC_START_VIEW.setVisibility(View.VISIBLE);
        } else if (event instanceof RecOnResponse) {
            REC_START_VIEW.setVisibility(View.GONE);
        } else if (event instanceof SpeakerOffResponse) {
            SPEAKER_ON_VIEW.setVisibility(View.VISIBLE);
            SPEAKER_OFF_VIEW.setVisibility(View.GONE);
        } else if (event instanceof SpeakerOnResponse) {
            SPEAKER_ON_VIEW.setVisibility(View.GONE);
            SPEAKER_OFF_VIEW.setVisibility(View.VISIBLE);
        }else{
            Log.i(TAG,"OutcomingCallActivity got undefind Event");
        }
    }
}
