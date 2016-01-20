package com.ammatti.stanley.sipdialer.dialer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ammatti.stanley.sipdialer.R;
import com.ammatti.stanley.sipdialer.SipApplication;
import com.ammatti.stanley.sipdialer.events.Event;
import com.ammatti.stanley.sipdialer.events.EventName;
import com.ammatti.stanley.sipdialer.events.requests.MakeCallRequest;
import com.ammatti.stanley.sipdialer.events.requests.RegUserRequest;
import com.ammatti.stanley.sipdialer.events.requests.UnRegUserRequest;
import com.ammatti.stanley.sipdialer.events.responses.CallStoppedResponse;
import com.ammatti.stanley.sipdialer.events.responses.UserRegisterResponse;
import com.ammatti.stanley.sipdialer.events.responses.UserUnRegisterResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Created by 1310042 on 2016/1/20.
 */
public class DialerFragment extends Fragment {

    public static final String TAG = "DialerFragment";

    public static final int REG_UPDATE = 1;
    public static final int CALL_END_UPDATE = 2;
    private Bus aTos_bus;
    private Bus sToa_bus;
    private String server_address_str = "";
    private String account_str = "";
    private String password_str = "";
    private String callee_number_str = "";

    private boolean reg_status;
    private TextView current_status;
    private EditText server_address;
    private EditText user_account;
    private EditText user_password;
    private EditText callee_number;
    private Button register_btn;
    private Button un_register_btn;
    private Button call_btn;

    Handler mUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == REG_UPDATE) {
                reg_status = SipApplication.getRegStatus();
                if (reg_status) {
                    current_status.setText(R.string.register);
                } else {
                    current_status.setText(R.string.unregister);
                }
            }else if(msg.what == CALL_END_UPDATE){
                //unbliock UI
                log("CALL_END_UPDATE");
                callee_number.setEnabled(true);
            }else{
                //undefined message
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialer_fragment_layout, null);

        current_status = (TextView) view.findViewById(R.id.status);
        server_address = (EditText) view.findViewById(R.id.address);
        user_account = (EditText) view.findViewById(R.id.account);
        user_password = (EditText) view.findViewById(R.id.password);
        callee_number = (EditText) view.findViewById(R.id.callee_number);

        register_btn = (Button) view.findViewById(R.id.btn_reg);
        if(register_btn == null){Log.i(TAG,"register_btn");}
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check server addresss, account, passwork
                server_address_str = server_address.getText().toString();
                account_str = user_account.getText().toString();
                password_str = user_password.getText().toString();
                //bliock UI
                if (!server_address_str.equals("") && !account_str.equals("") && !password_str.equals("")) {
                    register_btn.setEnabled(false);
                    un_register_btn.setEnabled(true);
                    server_address.setEnabled(false);
                    user_account.setEnabled(false);
                    user_password.setEnabled(false);
                    call_btn.setEnabled(true);
                    callee_number.setEnabled(true);
                    //sent Event
                    RegUserRequest reg_event = new RegUserRequest(EventName.REGISTER_USER_REQUEST);
                    reg_event.setServerAddress(server_address_str);
                    reg_event.setUserAccount(account_str);
                    reg_event.setUserPassword(password_str);
                    aTos_bus.post(reg_event);
                }
            }
        });
        un_register_btn = (Button) view.findViewById(R.id.btn_unreg);
        un_register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //unbliock UI
                register_btn.setEnabled(true);
                un_register_btn.setEnabled(false);
                call_btn.setEnabled(false);
                server_address.setEnabled(true);
                user_account.setEnabled(true);
                user_password.setEnabled(true);
                callee_number.setEnabled(false);
                //sent Event
                UnRegUserRequest unreg_event = new UnRegUserRequest(EventName.UNREGISTER_USER_REQUEST);
                aTos_bus.post(unreg_event);
            }
        });
        call_btn = (Button) view.findViewById(R.id.call);
        call_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check  callee number
                callee_number_str = callee_number.getText().toString();
                if (!callee_number_str.equals("")) {
                    //bliock UI
                    callee_number.setEnabled(false);
                    //sent Event
                    MakeCallRequest call_event = new MakeCallRequest(EventName.MAKE_CALL_REQUEST);
                    call_event.setCalleeNumber(callee_number_str);
                    aTos_bus.post(call_event);
                }
            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        log("onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        log("onResume");
        sToa_bus = SipApplication.getServiceToActivityBusInstance();
        aTos_bus = SipApplication.getActivityToServiceBusInstance();
        if(SipApplication.getStoaBusRegStatus() == false){
            sToa_bus.register(this);
            SipApplication.setStoaBusRegStatus(true);
        }
        reg_status = SipApplication.getRegStatus();
        if (reg_status) {
            current_status.setText(R.string.register);
        } else {
            current_status.setText(R.string.unregister);
        }
    }

    @Override
    public void onPause() {
        log("onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        log("onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(SipApplication.getStoaBusRegStatus() == true){
            sToa_bus.unregister(this);
            SipApplication.setStoaBusRegStatus(false);
        }
    }


    @Subscribe
    public void onEvent(Event event) {
        if (event instanceof UserRegisterResponse) {
            //send message to change UI
            Message msg = mUIHandler.obtainMessage();
            msg.what = REG_UPDATE;
            msg.sendToTarget();
        } else if (event instanceof UserUnRegisterResponse) {
            //send message to change UI
            Message msg = mUIHandler.obtainMessage();
            msg.what = REG_UPDATE;
            msg.sendToTarget();
        } else if (event instanceof CallStoppedResponse) {
            log("CallStoppedResponse");
            Message msg = mUIHandler.obtainMessage();
            msg.what = CALL_END_UPDATE;
            msg.sendToTarget();
        } else {
            //default value
            log(event.getEventName() + " is unhandled");
        }
    }

    private static void log(String log) {
        Log.i(TAG, log);
    }
}
