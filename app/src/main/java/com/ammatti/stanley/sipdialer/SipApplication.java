package com.ammatti.stanley.sipdialer;
import android.app.Application;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by user on 25.08.15.
 */
public class SipApplication extends Application {

    private static Bus bus = new Bus(ThreadEnforcer.ANY);

    public static Bus getBusInstance() {
        return bus;
    }

}
