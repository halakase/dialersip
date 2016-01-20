package com.ammatti.stanley.sipdialer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TabHost;

import com.ammatti.stanley.sipdialer.calllog.CallLogFragment;
import com.ammatti.stanley.sipdialer.contacts.ContactsFragment;
import com.ammatti.stanley.sipdialer.dialer.DialerFragment;
import com.ammatti.stanley.sipdialer.events.Event;
import com.ammatti.stanley.sipdialer.events.EventName;
import com.ammatti.stanley.sipdialer.events.requests.MakeCallRequest;
import com.ammatti.stanley.sipdialer.events.requests.RegUserRequest;
import com.ammatti.stanley.sipdialer.events.requests.UnRegUserRequest;
import com.ammatti.stanley.sipdialer.events.responses.CallStoppedResponse;
import com.ammatti.stanley.sipdialer.events.responses.UserRegisterResponse;
import com.ammatti.stanley.sipdialer.events.responses.UserUnRegisterResponse;
import com.ammatti.stanley.sipdialer.voicemail.VoiceMailFragment;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Created by 1310042 on 2015/9/22.
 */
public class SipActivity extends FragmentActivity {

    public static final String TAG = "SipActivity";

    private FragmentTabHost appTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sip_activity_layout);

        //init fragment layout
        InitView();
    }

    private void InitView() {
        appTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        appTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        LayoutInflater inflater = LayoutInflater.from(this);

        appTabHost.addTab(appTabHost.newTabSpec(getString(R.string.tab_contacts)).setIndicator(
                        createTabIndicator(inflater, appTabHost, R.string.tab_contacts, android.R.drawable.ic_menu_call)),
                ContactsFragment.class, null);
        appTabHost.addTab(appTabHost.newTabSpec(getString(R.string.tab_logs)).setIndicator(
                        createTabIndicator(inflater, appTabHost, R.string.tab_logs, android.R.drawable.ic_menu_recent_history)),
                CallLogFragment.class, null);
        appTabHost.addTab(appTabHost.newTabSpec(getString(R.string.tab_dialer)).setIndicator(
                        createTabIndicator(inflater, appTabHost, R.string.tab_dialer, android.R.drawable.ic_dialog_dialer)),
                DialerFragment.class, null);
        appTabHost.addTab(appTabHost.newTabSpec(getString(R.string.tab_voicemail)).setIndicator(
                        createTabIndicator(inflater, appTabHost, R.string.tab_voicemail, android.R.drawable.ic_media_play)),
                VoiceMailFragment.class, null);
        appTabHost.setCurrentTabByTag(getString(R.string.tab_dialer));
    }

    private View createTabIndicator(LayoutInflater inflater, TabHost tabHost, int textResource, int iconResource) {
        View tabIndicator = inflater.inflate(R.layout.tab_indicator, tabHost.getTabWidget(), false);
        ((TextView) tabIndicator.findViewById(android.R.id.title)).setText(textResource);
        ((ImageView) tabIndicator.findViewById(android.R.id.icon)).setImageResource(iconResource);
        return tabIndicator;
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
}
