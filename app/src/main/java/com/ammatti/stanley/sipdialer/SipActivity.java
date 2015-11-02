package com.ammatti.stanley.sipdialer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
 * Created by 1310042 on 2015/9/22.
 */
public class SipActivity extends Activity {

    public static final String TAG = "SipActivity";
    public static final int REG_UPDATE = 1;
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
            }else{
                //undefined message
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sip_activity_layout);

        current_status = (TextView) findViewById(R.id.status);

        server_address = (EditText) findViewById(R.id.address);
        user_account = (EditText) findViewById(R.id.account);
        user_password = (EditText) findViewById(R.id.password);
        callee_number = (EditText) findViewById(R.id.callee_number);

        register_btn = (Button) findViewById(R.id.btn_reg);
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
        un_register_btn = (Button) findViewById(R.id.btn_unreg);
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
        call_btn = (Button) findViewById(R.id.call);
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        sToa_bus = SipApplication.getServiceToActivityBusInstance();
        aTos_bus = SipApplication.getActivityToServiceBusInstance();
        sToa_bus.register(this);
        reg_status = SipApplication.getRegStatus();
        if (reg_status) {
            current_status.setText(R.string.register);
        } else {
            current_status.setText(R.string.unregister);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sToa_bus.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

        } else {
            //default value
            log(event.getEventName() + " is unhandled");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Show home screen when pressing "back" button,
            //  so that this app won't be closed accidentally
            Intent intentHome = new Intent(Intent.ACTION_MAIN);
            intentHome.addCategory(Intent.CATEGORY_HOME);
            intentHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentHome);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private static void log(String log) {
        Log.i(TAG, log);
    }
}
