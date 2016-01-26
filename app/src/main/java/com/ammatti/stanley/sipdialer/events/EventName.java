package com.ammatti.stanley.sipdialer.events;

/**
 * Created by 1310042 on 2015/9/22.
 */
public class EventName {
    //request
    public static final String ACCEPT_CALL_REQUEST = "AcceptCallRequest";
    public static final String DECLINE_CALL_REQUEST = "DeclineCallRequest";
    public static final String MAKE_CALL_REQUEST = "MakeCallRequest";
    public static final String STOP_CALL_REQUEST = "StopCallRequest";
    public static final String REGISTER_USER_REQUEST = "RegUserRequest";
    public static final String UNREGISTER_USER_REQUEST = "UnRegUserRequest";
    public static final String MIC_OFF_REQUEST = "MicOffRequest";
    public static final String MIC_ON_REQUEST = "MicOnRequest";
    public static final String REC_OFF_REQUEST = "RecOffRequest";
    public static final String REC_ON_REQUEST = "RecOnRequest";
    public static final String SPEAKER_OFF_REQUEST = "SpeakerOffRequest";
    public static final String SPEAKER_ON_REQUEST = "SpeakerOnRequest";

    //response
    public static final String CALL_ACCEPTEDRESPONSE = "CallAcceptedResponse";
    public static final String CALL_DECLINEDRESPONSE = "CallDeclinedResponse";
    public static final String CALL_PROGRESSRESPONSE = "CallInProgressResponse";
    public static final String CALL_STARTEDRESPONSE = "CallStartedResponse";
    public static final String CALL_STOPPEDRESPONSE = "CallStoppedResponse";
    public static final String USER_REGISTERRESPONSE = "UserRegisterResponse";
    public static final String USER_UNREGISTERRESPONSE = "UserUnRegisterResponse";
    public static final String SHOW_SCREENINCOMMINGRESPONSE = "ShowScreenIncomingResponse";
    public static final String SHOW_SCREENOUTCOMMINGRESPONSE = "ShowScreenOutcomingResponse";
    public static final String MIC_OFF_RESPONSE = "MicOffResponse";
    public static final String MIC_ON_RESPONSE = "MicOnResponse";
    public static final String REC_OFF_RESPONSE = "RecOffResponse";
    public static final String REC_ON_RESPONSE = "RecOnResponse";
    public static final String SPEAKER_OFF_RESPONSE = "SpeakerOffResponse";
    public static final String SPEAKER_ON_RESPONSE = "SpeakerOnResponse";

    //others
    public static final String SURFACEVIEW_EVENT = "SurfaceViewEvent";
    public static final String ANDROIDVIDEOWIN_EVENT = "AndroidVideoWindowImplEvent";
}
