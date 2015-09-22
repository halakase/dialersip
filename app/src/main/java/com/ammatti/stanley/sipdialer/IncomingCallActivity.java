package com.ammatti.stanley.sipdialer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ammatti.stanley.sipdialer.events.EventName;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.ammatti.stanley.sipdialer.events.requests.StopCallRequest;
import com.ammatti.stanley.sipdialer.events.responses.CallAcceptedResponse;
import com.ammatti.stanley.sipdialer.events.responses.CallDeclinedResponse;
import com.ammatti.stanley.sipdialer.events.responses.CallStartedResponse;
import com.ammatti.stanley.sipdialer.events.responses.CallStoppedResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.MicOffResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.MicOnResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.RecOffResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.RecOnResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.SpeakerOffResponse;
import com.ammatti.stanley.sipdialer.events.responses.dev.SpeakerOnResponse;

/**
 * Created by user on 25.08.15.
 */
public class IncomingCallActivity extends Activity {

    Bus bus = SipApplication.getBusInstance();// todo: REGISTER INSTANCES OF BUS OBJECTS

    private LinearLayout ringerLayout;

    private int ACCEPT_INCOMING_VIEW_ID = R.id.imageView10;
    private int DENY_INCOMING_VIEW_ID = R.id.imageView2;
    private int DENY_CALL_VIEW_ID = R.id.imageView9;
    private int FOOTER_ID = R.id.footer_rel;
    private int TIME_TEXT_VIEW_ID = R.id.textView4;
    private int MIC_TURN_OFF_ID = R.id.imageView3;
    private int MIC_TURN_ON_ID = R.id.imageView11;
    private int REC_START_ID = R.id.imRecStart;
    private int REC_STOP_ID = R.id.imgRecStop;
    private int SPEAKER_ON_ID = R.id.imageView14;
    private int SPEAKER_OFF_ID = R.id.imageView15;

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

                bus.post(new CallAcceptedResponse(EventName.CALL_ACCEPTEDRESPONSE));

                //isInCall = true;

                vibrator.cancel();
                stopBeep();

            } else if (view.getId() == DENY_INCOMING_VIEW_ID) { // decline incoming

                bus.post(new CallDeclinedResponse(EventName.CALL_DECLINEDRESPONSE));

                vibrator.cancel();
                stopBeep();

            } else if (view.getId() == DENY_CALL_VIEW_ID) { // decline current call

                bus.post(new StopCallRequest(EventName.STOP_CALL_REQUEST));

                //isInCall = false;

                vibrator.cancel();
                stopBeep();

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
        initLayout();
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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // note: this is an exclusive case when Activity receives incoming call event while it is in call or near it(dialing or nearly received new one)
    }

    private void initLayout(){
        setContentView(R.layout.incomingcall_activity_layout);
        ringerLayout = (LinearLayout) findViewById(R.id.root_root);

        ACCEPT_INCOMING_VIEW = (ImageView) ringerLayout.findViewById(ACCEPT_INCOMING_VIEW_ID);
        DENY_INCOMING_VIEW =(ImageView) ringerLayout.findViewById(DENY_INCOMING_VIEW_ID);
        DENY_CALL_VIEW =(ImageView) ringerLayout.findViewById(DENY_CALL_VIEW_ID);
        MIC_TURN_OFF_VIEW =(ImageView) ringerLayout.findViewById(MIC_TURN_OFF_ID);
        MIC_TURN_ON_VIEW =(ImageView) ringerLayout.findViewById(MIC_TURN_ON_ID);
        REC_STOP_VIEW =(ImageView) ringerLayout.findViewById(REC_STOP_ID);
        REC_START_VIEW =(ImageView) ringerLayout.findViewById(REC_START_ID);
        SPEAKER_ON_VIEW =(ImageView) ringerLayout.findViewById(SPEAKER_ON_ID);
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

        USER_NAME_VIEW = (TextView) ringerLayout.findViewById(R.id.textView);
        TIMER_TEXT_VIEW = (TextView) ringerLayout.findViewById(R.id.textView4);
        CALL_TYPE_TEXT_VIEW = (TextView) ringerLayout.findViewById(R.id.textView5);

        FOOTER = (RelativeLayout) ringerLayout.findViewById(R.id.footer_rel);

        // todo: SETUP REMOTE USER NAME
        String name = "some name";

        USER_NAME_VIEW.setText(name);

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
    public void onEvent(Object event) {
        if (event instanceof CallStartedResponse) {
            startTimer();
        } else if (event instanceof CallStoppedResponse) {
            stopTimer();
        }

        else if (event instanceof MicOffResponse) {
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
        }
    }

}
