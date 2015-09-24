package com.ammatti.stanley.sipdialer;
import android.app.Application;
import android.widget.Button;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by user on 25.08.15.
 */
public class SipApplication extends Application {

    private static Bus bus = new Bus(ThreadEnforcer.ANY);

    private static boolean register_status = false;

    public static Bus getBusInstance() {
        return bus;
    }

    public static void setRegStatus(boolean value){
        register_status= value;
    }

    public static boolean getRegStatus(){
        return register_status;
    }
}
