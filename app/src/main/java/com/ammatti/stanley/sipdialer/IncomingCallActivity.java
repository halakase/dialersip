package com.ammatti.stanley.sipdialer;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ammatti.stanley.sipdialer.events.Event;
import com.ammatti.stanley.sipdialer.events.EventName;
import com.ammatti.stanley.sipdialer.events.requests.AcceptCallRequest;
import com.ammatti.stanley.sipdialer.events.requests.DeclineCallRequest;
import com.ammatti.stanley.sipdialer.events.requests.StopCallRequest;
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

/**
 * Created by user on 25.08.15.
 */
public class IncomingCallActivity extends Activity {

    private static final String TAG = "IncomingCallActivity";
    private Bus aTos_bus;
    private Bus sToa_bus;
    private LinearLayout ringerLayout;
    private int ACCEPT_INCOMING_VIEW_ID;
    private int DENY_INCOMING_VIEW_ID;
    private int DENY_CALL_VIEW_ID;
    private int FOOTER_ID;
    private int TIME_TEXT_VIEW_ID;
    private int MIC_TURN_OFF_ID;
    private int MIC_TURN_ON_ID;
    private int REC_START_ID;
    private int REC_STOP_ID;
    private int SPEAKER_ON_ID;
    private int SPEAKER_OFF_ID;

    private ImageView ACCEPT_INCOMING_VIEW;
    private ImageView DENY_INCOMING_VIEW;
    private ImageView DENY_CALL_VIEW;
    private ImageView MIC_TURN_OFF_VIEW;
    private ImageView MIC_TURN_ON_VIEW;
    private ImageView REC_START_VIEW;
    private ImageView REC_STOP_VIEW;
    private ImageView SPEAKER_ON_VIEW;
    private ImageView SPEAKER_OFF_VIEW;

    private RelativeLayout FOOTER;

    private TextView USER_NAME_VIEW;
    private static TextView TIMER_TEXT_VIEW;
    private TextView CALL_TYPE_TEXT_VIEW;

    private Handler handler = new Handler();
    private boolean isTimerStarted;
    private boolean stopTimer;
    static int currentTimer = 0;

    // Get instance of Vibrator from current Context
    private Vibrator vibrator;

    // Get instance of MediaPlayer from current Context
    private MediaPlayer mMediaPlayer;

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
            if (view.getId() == ACCEPT_INCOMING_VIEW_ID) { // accept incoming
                FOOTER.setVisibility(View.VISIBLE);
                ACCEPT_INCOMING_VIEW.setVisibility(View.GONE);
                DENY_CALL_VIEW.setVisibility(View.VISIBLE);
                DENY_INCOMING_VIEW.setVisibility(View.GONE);
                // make timer visible
                TIMER_TEXT_VIEW.setVisibility(View.VISIBLE);
                aTos_bus.post(new AcceptCallRequest(EventName.ACCEPT_CALL_REQUEST));
                //isInCall = true;
                vibrator.cancel();
                stopBeep();
            } else if (view.getId() == DENY_INCOMING_VIEW_ID) { // decline incoming
                aTos_bus.post(new DeclineCallRequest(EventName.DECLINE_CALL_REQUEST));
                vibrator.cancel();
                stopBeep();
                IncomingCallActivity.this.finish();
            } else if (view.getId() == DENY_CALL_VIEW_ID) { // decline current call
                aTos_bus.post(new StopCallRequest(EventName.STOP_CALL_REQUEST));
                //isInCall = false;
                vibrator.cancel();
                stopBeep();
                IncomingCallActivity.this.finish();
            } else if (view.getId() == MIC_TURN_OFF_ID) { // decline incoming
                MIC_TURN_OFF_VIEW.setVisibility(View.GONE);
                MIC_TURN_ON_VIEW.setVisibility(View.VISIBLE);
                /*not implemented*/
            } else if (view.getId() == MIC_TURN_ON_ID) { // decline incoming
                MIC_TURN_OFF_VIEW.setVisibility(View.VISIBLE);
                MIC_TURN_ON_VIEW.setVisibility(View.GONE);
                /*not implemented*/
            } else if (view.getId() == REC_START_ID) { // decline incoming
                REC_START_VIEW.setVisibility(View.GONE);
                /*not implemented*/
            } else if (view.getId() == REC_STOP_ID) { // decline incoming
                REC_START_VIEW.setVisibility(View.VISIBLE);
                /*not implemented*/
            } else if (view.getId() == SPEAKER_ON_ID) { // decline incoming
                SPEAKER_ON_VIEW.setVisibility(View.GONE);
                SPEAKER_OFF_VIEW.setVisibility(View.VISIBLE);
                /*not implemented*/
            } else if (view.getId() == SPEAKER_OFF_ID) { // decline incoming
                SPEAKER_ON_VIEW.setVisibility(View.VISIBLE);
                SPEAKER_OFF_VIEW.setVisibility(View.GONE);
                /*not implemented*/
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        String caller = bundle.getString("caller");
        vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        mMediaPlayer =  MediaPlayer.create(this, getSystemDefultRingtoneUri());
        initLayout(caller);
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
        super.onPause();
        isTimerStarted = false;
        currentTimer = 0;
        stopTimer = true;
        handler.removeCallbacks(updateTimer);
        vibrator.cancel();
        stopBeep();
        sToa_bus.unregister(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // note: this is an exclusive case when Activity receives incoming call event while it is in call or near it(dialing or nearly received new one)
    }

    private void initLayout(String caller) {
        setContentView(R.layout.incomingcall_activity_layout);
        ACCEPT_INCOMING_VIEW_ID = R.id.accept_call;
        DENY_INCOMING_VIEW_ID = R.id.reject_call;
        DENY_CALL_VIEW_ID = R.id.handoff_call;
        FOOTER_ID = R.id.footer_rel;
        TIME_TEXT_VIEW_ID = R.id.time;
        MIC_TURN_OFF_ID = R.id.mic_off;
        MIC_TURN_ON_ID = R.id.mic_on;
        REC_START_ID = R.id.imRecStart;
        REC_STOP_ID = R.id.imgRecStop;
        SPEAKER_ON_ID = R.id.speaker_on;
        SPEAKER_OFF_ID = R.id.speaker_off;

        ringerLayout = (LinearLayout) findViewById(R.id.root_root);

        ACCEPT_INCOMING_VIEW = (ImageView) ringerLayout.findViewById(ACCEPT_INCOMING_VIEW_ID);
        DENY_INCOMING_VIEW = (ImageView) ringerLayout.findViewById(DENY_INCOMING_VIEW_ID);
        DENY_CALL_VIEW = (ImageView) ringerLayout.findViewById(DENY_CALL_VIEW_ID);
        MIC_TURN_OFF_VIEW = (ImageView) ringerLayout.findViewById(MIC_TURN_OFF_ID);
        MIC_TURN_ON_VIEW = (ImageView) ringerLayout.findViewById(MIC_TURN_ON_ID);
        REC_STOP_VIEW = (ImageView) ringerLayout.findViewById(REC_STOP_ID);
        REC_START_VIEW = (ImageView) ringerLayout.findViewById(REC_START_ID);
        SPEAKER_ON_VIEW = (ImageView) ringerLayout.findViewById(SPEAKER_ON_ID);
        SPEAKER_OFF_VIEW = (ImageView) ringerLayout.findViewById(SPEAKER_OFF_ID);

        ringerLayout.findViewById(ACCEPT_INCOMING_VIEW_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(DENY_INCOMING_VIEW_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(DENY_CALL_VIEW_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(MIC_TURN_OFF_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(MIC_TURN_ON_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(REC_STOP_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(REC_START_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(SPEAKER_ON_ID).setOnClickListener(inCallListener);
        ringerLayout.findViewById(SPEAKER_OFF_ID).setOnClickListener(inCallListener);

        USER_NAME_VIEW = (TextView) ringerLayout.findViewById(R.id.caller_name);
        TIMER_TEXT_VIEW = (TextView) ringerLayout.findViewById(TIME_TEXT_VIEW_ID);
        CALL_TYPE_TEXT_VIEW = (TextView) ringerLayout.findViewById(R.id.called_label);

        FOOTER = (RelativeLayout) ringerLayout.findViewById(R.id.footer_rel);

        // SETUP REMOTE USER NAME
        USER_NAME_VIEW.setText(caller);

        ringerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Start without a delay
                // Vibrate for 100 milliseconds
                // Sleep for 1000 milliseconds
                long[] pattern = {0, 1800, 1600};

                // The '0' here means to repeat indefinitely
                // '0' is actually the index at which the pattern keeps repeating from (the start)
                // To repeat the pattern from any other point, you could increase the index, e.g. '1'
                vibrator.vibrate(pattern, 0);

                playBeep();

                ringerLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    private Uri getSystemDefultRingtoneUri() {
        return RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_RINGTONE);
    }

    public void playBeep() {
        try {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = new MediaPlayer();
            }

            AssetFileDescriptor descriptor = this.getAssets().openFd("Nokia_tune_ogg.ogg");
            mMediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            mMediaPlayer.prepare();
            mMediaPlayer.setVolume(1f, 1f);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopBeep() {
        try {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
        } catch (IllegalStateException exc) {
            exc.printStackTrace();
        }
    }

    private void startTimer() {
        if (!isTimerStarted) {
            isTimerStarted = true;
            if (vibrator != null)
                vibrator.cancel();

            handler.post(updateTimer);
        }
    }

    private void stopTimer() {
        isTimerStarted = false;
        // todo: stop timer
    }

    @Subscribe
    public void onEvent(Event event) {
        if (event instanceof CallAcceptedResponse) {
            startTimer();
            //set up audio

        } else if (event instanceof CallStoppedResponse) {
            Log.i(TAG, "IncomingCallActivity got CallStoppedResponse");
            stopTimer();
            //release audio
        } else if (event instanceof CallDeclinedResponse) {
            Log.i(TAG, "IncomingCallActivity got CallDeclinedResponse");
            stopTimer();
            //release audio
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
            Log.i(TAG,"IncomingCallActivity got undefind Event");
        }
    }

}
