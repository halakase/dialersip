package com.ammatti.stanley.sipdialer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ammatti.stanley.sipdialer.events.Event;
import com.ammatti.stanley.sipdialer.events.EventName;
import com.ammatti.stanley.sipdialer.events.requests.AcceptCallRequest;
import com.ammatti.stanley.sipdialer.events.requests.DeclineCallRequest;
import com.ammatti.stanley.sipdialer.events.requests.MakeCallRequest;
import com.ammatti.stanley.sipdialer.events.requests.RegUserRequest;
import com.ammatti.stanley.sipdialer.events.requests.StopCallRequest;
import com.ammatti.stanley.sipdialer.events.requests.UnRegUserRequest;
import com.ammatti.stanley.sipdialer.events.requests.dev.MicOffRequest;
import com.ammatti.stanley.sipdialer.events.requests.dev.MicOnRequest;
import com.ammatti.stanley.sipdialer.events.requests.dev.RecOffRequest;
import com.ammatti.stanley.sipdialer.events.requests.dev.RecOnRequest;
import com.ammatti.stanley.sipdialer.events.requests.dev.SpeakerOffRequest;
import com.ammatti.stanley.sipdialer.events.requests.dev.SpeakerOnRequest;
import com.ammatti.stanley.sipdialer.events.responses.CallAcceptedResponse;
import com.ammatti.stanley.sipdialer.events.responses.CallDeclinedResponse;
import com.ammatti.stanley.sipdialer.events.responses.CallInProgressResponse;
import com.ammatti.stanley.sipdialer.events.responses.CallStoppedResponse;
import com.ammatti.stanley.sipdialer.events.responses.ShowScreenIncomingResponse;
import com.ammatti.stanley.sipdialer.events.responses.ShowScreenOutcomingResponse;
import com.ammatti.stanley.sipdialer.events.responses.UserRegisterResponse;
import com.ammatti.stanley.sipdialer.events.responses.UserUnRegisterResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallStats;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneEvent;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneInfoMessage;
import org.linphone.core.LinphoneLogHandler;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.Reason;
import org.linphone.core.SubscriptionState;

import java.nio.ByteBuffer;

/**
 * Created by user on 25.08.15.
 */
public class SipService extends Service {

    public static final String TAG = "SipService";
    public Bus aTos_bus = null;
    public Bus sToa_bus = null;

    private LinphoneCore lc;
    private LinphoneCoreFactory mLcFactory;
    private LinphoneProxyConfig mProxyCfg;
    private boolean mRunning;
    private boolean mAudioFocused = false;
    private AudioManager mAudioManager;

    private LinphoneLogHandler mLinphonehandler = new LinphoneLogHandler() {
        @Override
        public void log(String loggerName, int level, String levelString, String msg, Throwable e) {
            Log.e("LinphoneHandler", "" + levelString + " msg=" + msg);//todo: remove logs
        }
    };

    private LinphoneCall currentCall;

    private LinphoneCoreListener mLinphoneListener = new LinphoneCoreListener() {

        @Override
        public void displayMessage(LinphoneCore lc, String message) {
            log("displayMessage: " + message);
        }

        @Override
        public void displayStatus(LinphoneCore lc, String message) {
            log("displayStatus: " + message);
            //handleEvent(lc, message);
        }

        @Override
        public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String message) {
            log("callState: " + state + " " + message + "; LinphoneCall call.getState(): " + call.getState());
            currentCall = call;

            if (state == LinphoneCall.State.OutgoingInit) {
                // stub
                log("State OutgoingInit");
                aTos_bus.post(new ShowScreenOutcomingResponse(EventName.SHOW_SCREENOUTCOMMINGRESPONSE));
            } else if (state == LinphoneCall.State.OutgoingProgress) {
                log("State OutgoingProgress");
                // stub
            } else if (state == LinphoneCall.State.Connected) {
                // stub
                log("State Connected");
                requestAudioFocus();
                setAudioManagerInCallMode(mAudioManager);
            } else if (state == LinphoneCall.State.CallEnd) {
                log("State CallEnd");
                if (currentCall != null) {
                    log("Terminating the call");
                    lc.terminateCall(currentCall);
                    if (mAudioFocused) {
                        int res = mAudioManager.abandonAudioFocus(null);
                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                        mAudioFocused = false;
                    }
                }
                aTos_bus.post(new StopCallRequest(EventName.STOP_CALL_REQUEST));
            } else if (state == LinphoneCall.State.CallReleased || state == LinphoneCall.State.Error) {
                log("State CallReleased");
                if (currentCall != null) {
                    log("Terminating the call");
                    lc.terminateCall(currentCall);
                    if (mAudioFocused) {
                        int res = mAudioManager.abandonAudioFocus(null);
                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                        mAudioFocused = false;
                    }
                }

                aTos_bus.post(new StopCallRequest(EventName.STOP_CALL_REQUEST));
            } else if (state == LinphoneCall.State.IncomingReceived) {
                log("State IncomingReceived");
                log(call.getRemoteContact());
                String uName = call.getRemoteContact().split(":")[1].split("@")[0];
                ShowScreenIncomingResponse callin_event = new ShowScreenIncomingResponse(EventName.SHOW_SCREENINCOMMINGRESPONSE);
                callin_event.setCallerNumber(uName);
                aTos_bus.post(callin_event);

            } else if (call.getState() == LinphoneCall.State.StreamsRunning) {
                log("State StreamsRunning");
                if (call.getRemoteContact() != null) {

                    //bus.post(new CallStartedEvent());//todo: replace this line

                    // note: this event is sending permanently
                    aTos_bus.post(new CallInProgressResponse(EventName.CALL_PROGRESSRESPONSE));

                } else {
                    Log.e("CALL_STATE", "LinphoneCall.State.OutgoingProgress call.getRemoteContact()==null");
                }
            } else if (message.startsWith("You have missed")) {
                String[] result = (message).split(" ");
                log("You have missed " + Integer.valueOf(result[3]));

                // todo: show notification
            } else {
                log("Bypass this Call State");
            }
        }

        @Override
        public void authInfoRequested(LinphoneCore lc, String realm, String username, String Domain) {
            log("authInfoRequested: " + username + " " + Domain + " " + realm);
        }

        @Override
        public void callStatsUpdated(LinphoneCore lc, LinphoneCall call, LinphoneCallStats stats) {
            log("callStatsUpdated: " + stats + "; LinphoneCall call.getState(): " + call.getState());
            currentCall = call;
        }

        @Override
        public void newSubscriptionRequest(LinphoneCore lc, LinphoneFriend lf, String url) {
            log("newSubscriptionRequest: " + url);
        }

        @Override
        public void notifyPresenceReceived(LinphoneCore lc, LinphoneFriend lf) {
            log("notifyPresenceReceived");
        }

        @Override
        public void textReceived(LinphoneCore lc, LinphoneChatRoom cr, LinphoneAddress from, String message) {
            log("textReceived: " + message + " ; from: " + from.asString());
        }

        @Override
        public void dtmfReceived(LinphoneCore lc, LinphoneCall call, int dtmf) {
            log("dtmfReceived: " + call.getRemoteAddress().asString());
            currentCall = call;
        }

        @Override
        public void notifyReceived(LinphoneCore lc, LinphoneCall call, LinphoneAddress from, byte[] event) {
            log("notifyReceived: from " + from.asString() + "; LinphoneCall call.getState(): " + call.getState());
            currentCall = call;
        }

        @Override
        public void transferState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State new_call_state) {
            log("transferState: " + new_call_state + "; LinphoneCall call.getState(): " + call.getState());
            currentCall = call;
        }

        @Override
        public void infoReceived(LinphoneCore lc, LinphoneCall call, LinphoneInfoMessage info) {
            log("infoReceived: " + info.getContent().getDataAsString() + "; LinphoneCall call.getState(): " + call.getState());
            currentCall = call;
        }

        @Override
        public void subscriptionStateChanged(LinphoneCore lc, LinphoneEvent ev, SubscriptionState state) {
            log("subscriptionStateChanged: " + state);
        }

        @Override
        public void publishStateChanged(LinphoneCore lc, LinphoneEvent ev, PublishState state) {
            log("publishStateChanged: " + state);
        }

        @Override
        public void show(LinphoneCore lc) {
            log("show");
        }

        @Override
        public void displayWarning(LinphoneCore lc, String message) {
            log("displayWarning: " + message);
        }

        @Override
        public void fileTransferProgressIndication(LinphoneCore lc, LinphoneChatMessage message, LinphoneContent content, int progress) {
            log("fileTransferProgressIndication: " + message + "; progress: " + progress);
        }

        @Override
        public void fileTransferRecv(LinphoneCore lc, LinphoneChatMessage message, LinphoneContent content, byte[] buffer, int size) {
            log("fileTransferRecv: " + message);
        }

        @Override
        public int fileTransferSend(LinphoneCore lc, LinphoneChatMessage message, LinphoneContent content, ByteBuffer buffer, int size) {
            log("fileTransferSend: " + message);
            return 0;
        }

        @Override
        public void globalState(LinphoneCore lc, LinphoneCore.GlobalState state, String message) {
            log("globalState: " + state + "; msg: " + message);
        }

        @Override
        public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg, LinphoneCore.RegistrationState state, String smessage) {
            log("registrationState: " + state + "; smessage: " + smessage);
            boolean reg_status = SipApplication.getRegStatus();
            if (state == LinphoneCore.RegistrationState.RegistrationOk) {
                if (reg_status == false) {
                    SipApplication.setRegStatus(true);
                }
            } else if (state == LinphoneCore.RegistrationState.RegistrationProgress) {
                //do nothing
            } else {
                if (reg_status == true) {
                    SipApplication.setRegStatus(false);
                }
            }
        }

        @Override
        public void configuringStatus(LinphoneCore lc, LinphoneCore.RemoteProvisioningState state, String message) {
            log("configuringStatus: " + state + "; message: " + message);
        }

        @Override
        public void messageReceived(LinphoneCore lc, LinphoneChatRoom cr, LinphoneChatMessage message) {
            log("messageReceived: " + message.getText());
        }

        @Override
        public void callEncryptionChanged(LinphoneCore lc, LinphoneCall call, boolean encrypted, String authenticationToken) {
            log("callEncryptionChanged, LinphoneCall call.getState(): " + call.getState() + "; encrypted: " + encrypted + "; authenticationToken: " + authenticationToken);
            currentCall = call;
        }

        @Override
        public void notifyReceived(LinphoneCore lc, LinphoneEvent ev, String eventName, LinphoneContent content) {
            log("notifyReceived: " + eventName + "; LinphoneContent: " + content.getDataAsString());
        }

        @Override
        public void isComposingReceived(LinphoneCore lc, LinphoneChatRoom cr) {
            log("isComposingReceived");
        }

        @Override
        public void ecCalibrationStatus(LinphoneCore lc, LinphoneCore.EcCalibratorStatus status, int delay_ms, Object data) {
            log("ecCalibrationStatus: " + status);
        }

        @Override
        public void uploadProgressIndication(LinphoneCore lc, int offset, int total) {
            log("uploadProgressIndication");
        }

        @Override
        public void uploadStateChanged(LinphoneCore lc, LinphoneCore.LogCollectionUploadState state, String info) {
            log("uploadStateChanged: " + state);
        }

        private void requestAudioFocus() {
            if (!mAudioFocused) {
                int res = mAudioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                // Log.d("Audio focus requested: " + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "Granted" : "Denied"));
                if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true;
            }
        }

        private void setAudioManagerInCallMode(AudioManager manager) {
            if (manager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
                return;
            }
            manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }
    };

    private Thread sipThread = new Thread(new Runnable() {
        @Override
        public void run() {
            mRunning = true;
            while (mRunning) {
                try {
                    lc.iterate();
                    Thread.sleep(50);
                } catch (RuntimeException exc) {
                    Log.e("SIP_SERVIC NATEXCEPTION", "");//todo: remove logs
                } catch (InterruptedException e) {
                    log("InterruptedException e: " + (e.getMessage() == null ? "null" : e.getMessage()));
                }
            }
        }
    });

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRunning = false;
    }

    public void init() {
        if (sToa_bus == null) {
            //get bus
            sToa_bus = SipApplication.getServiceToActivityBusInstance();
        }

        if (aTos_bus == null) {
            //get bus
            aTos_bus = SipApplication.getActivityToServiceBusInstance();
            // registering bus
            aTos_bus.register(this);
        }
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        try {
            mLcFactory = LinphoneCoreFactory.instance();
            mLcFactory.setLogHandler(mLinphonehandler);
            mLcFactory.setDebugMode(true, "linphone_handler_core_debug_temp");
            mLcFactory.setLogCollectionPath(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/linphone_handler" + System.currentTimeMillis() + ".txt");
            lc = mLcFactory.createLinphoneCore(mLinphoneListener, null);
            if (!sipThread.isAlive()) {
                sipThread.start();
            }
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        return START_STICKY;//todo: service could be killed anyway
    }

    private void startIncomingActivity(String caller_number) {
        Intent startIncomingCallActivtiy = new Intent(this, IncomingCallActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("caller", caller_number);
        startIncomingCallActivtiy.setAction(Intent.ACTION_VIEW);
        startIncomingCallActivtiy.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startIncomingCallActivtiy.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startIncomingCallActivtiy.putExtras(bundle);
        this.getApplicationContext().startActivity(startIncomingCallActivtiy);
    }

    private void startOutcomingActivity() {
        Intent startOutcomingCallActivtiy = new Intent(this, OutcomingCallActivity.class);
        Bundle bundle = new Bundle();

        startOutcomingCallActivtiy.setAction(Intent.ACTION_VIEW);
        startOutcomingCallActivtiy.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startOutcomingCallActivtiy.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startOutcomingCallActivtiy.putExtras(bundle);
        this.getApplicationContext().startActivity(startOutcomingCallActivtiy);
    }

    @Subscribe
    public void onEvent(Event event) {
        if (event instanceof AcceptCallRequest) {
            acceptIncoming();
        } else if (event instanceof DeclineCallRequest) {
            declineIncoming();
        } else if (event instanceof MakeCallRequest) {
            String callee_num = ((MakeCallRequest) event).getCalleeNumber();
            inviteInCall(callee_num);
        } else if (event instanceof RegUserRequest) {
            String server_addr = ((RegUserRequest) event).getServerAddress();
            String account = ((RegUserRequest) event).getUserAccount();
            String password = ((RegUserRequest) event).getUserPassword();
            registerUser(server_addr, account, password);
        } else if (event instanceof UnRegUserRequest) {
            unregisterUser();
        } else if (event instanceof StopCallRequest) {
            stopCall();
        } else if (event instanceof MicOffRequest) {
            // stub: further development
        } else if (event instanceof MicOnRequest) {
            // stub: further development
        } else if (event instanceof RecOffRequest) {
            // stub: further development
        } else if (event instanceof RecOnRequest) {
            // stub: further development
        } else if (event instanceof SpeakerOffRequest) {
            // stub: further development
        } else if (event instanceof SpeakerOnRequest) {
            // stub: further development
        } else if (event instanceof ShowScreenIncomingResponse) {
            String caller_num = ((ShowScreenIncomingResponse) event).getCallerNumber();
            startIncomingActivity(caller_num);
        } else if (event instanceof ShowScreenOutcomingResponse) {
            startOutcomingActivity();
        }
    }

    private void acceptIncoming() {
        try {
            lc.acceptCall(currentCall);
            sToa_bus.post(new CallAcceptedResponse(EventName.CALL_ACCEPTEDRESPONSE));
        } catch (LinphoneCoreException exc) {
            sToa_bus.post(new CallDeclinedResponse(EventName.CALL_DECLINEDRESPONSE));
            log("LinphoneCoreException: " + (exc.getMessage() == null ? "LinphoneCoreException is null" : exc.getMessage()));
        }
    }

    private void declineIncoming() {
        try {
            lc.declineCall(currentCall, Reason.Declined);
        } catch (NullPointerException e) {
            Log.e("SipService", "NPE");//todo: remove logs
            log("SipService receivedInCallStopped NPE");
        }
        sToa_bus.post(new CallDeclinedResponse(EventName.CALL_DECLINEDRESPONSE));
    }

    private void inviteInCall(String callee_number) {
        try {
            LinphoneAddress addr = mLcFactory.createLinphoneAddress("sip:" + callee_number);
            currentCall = lc.invite(addr);

            log("Call created");
            //bus.post(new ShowOutgoingEvent);
            // do not send ShowOutgoingEvent now, send it in "outgoingInit"
            // show it in callback

        } catch (Exception e) {
            e.printStackTrace();
            log("Invalid sip address: " + callee_number + ". " + e.getMessage());

        }
    }

    private void stopCall() {
        lc.terminateCall(currentCall);
        sToa_bus.post(new CallStoppedResponse(EventName.CALL_STOPPEDRESPONSE));
    }

    private void registerUser(String server_addr, String account, String password) {
        String server_address = server_addr;
        String user_account = account;
        String user_password = password;
        try {
            String realm = "sip:" + server_address;
            LinphoneAddress address = mLcFactory.createLinphoneAddress(realm);
            address.setTransport(LinphoneAddress.TransportType.LinphoneTransportTcp);
            //String domain = address.getDomain();
            lc.addAuthInfo(mLcFactory.createAuthInfo(user_account, user_password, null, server_addr));
            //mProxyCfg = lc.createProxyConfig(realm, domain, null, true);
            mProxyCfg = lc.createProxyConfig("sip:" + user_account + "@" + server_addr, address.asStringUriOnly(), address.asStringUriOnly(), false);
            mProxyCfg.setExpires(3600);
            lc.addProxyConfig(mProxyCfg);
            lc.setDefaultProxyConfig(mProxyCfg);
            LinphoneProxyConfig lpc = lc.getDefaultProxyConfig();
            if (lpc != null) {
                lpc.edit();
                //lpc.enableAvpf(false);
                //lpc.setAvpfRRInterval(5);
                lpc.setExpires(3600);
                lpc.enableRegister(true);
                lpc.done();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (lc.getDefaultProxyConfig().getState() != LinphoneCore.RegistrationState.RegistrationOk) {
                        lc.iterate();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(SipApplication.getRegStatus() == true){
                        log("Registration Created");
                        UserRegisterResponse reg_rsp_event = new UserRegisterResponse(EventName.USER_REGISTERRESPONSE);
                        sToa_bus.post(reg_rsp_event);
                    }else{
                        log("Registration Failed");
                    }
                }
            }).start();
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
            log("Invalid user address: " + server_address + ": " + e.getMessage());
            throw new RuntimeException(e.getMessage() == null ? "LinphoneCoreException" : e.getMessage());
        }
    }

    private void unregisterUser() {
        if (mProxyCfg == null) {
            log("Already unregistered!");
            return;
        }

        mProxyCfg.edit();
        mProxyCfg.enableRegister(false);
        mProxyCfg.done();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (lc.getDefaultProxyConfig().getState() != LinphoneCore.RegistrationState.RegistrationCleared) {
                    lc.iterate();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(SipApplication.getRegStatus() == false){
                    log("Unregistered !!");
                    UserUnRegisterResponse unreg_rsp_event = new UserUnRegisterResponse(EventName.USER_UNREGISTERRESPONSE);
                    sToa_bus.post(unreg_rsp_event);
                }else{
                    log("Unregistered Failed");
                }
            }
        }).start();
    }


    //---------------------------------------------------------------------------------------------/
    // helpful methods
    // ok, at least seemed so
    private boolean isInCall(LinphoneCall call) {
        if (call == null)
            return false;

        if (call.getState() == LinphoneCall.State.IncomingReceived
                || call.getState() == LinphoneCall.State.OutgoingRinging
                || call.getState() == LinphoneCall.State.OutgoingInit
                || call.getState() == LinphoneCall.State.Paused
                || call.getState() == LinphoneCall.State.Pausing
                || call.getState() == LinphoneCall.State.CallUpdating
                || call.getState() == LinphoneCall.State.Resuming
                || call.getState() == LinphoneCall.State.StreamsRunning
                || call.getState() == LinphoneCall.State.OutgoingProgress) {
            return true;
        }
        return false;
    }

    private static void log(String log) {
        Log.i(TAG, log);
    }
}
