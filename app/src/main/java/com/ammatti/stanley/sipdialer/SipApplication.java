package com.ammatti.stanley.sipdialer;

import android.app.Application;
import android.widget.Button;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by user on 25.08.15.
 */
public class SipApplication extends Application {

    //private static Bus bus = new Bus(ThreadEnforcer.ANY);

    private static Bus serviceToActivity_bus = new Bus(ThreadEnforcer.ANY);
    private static Bus activityToService_bus = new Bus(ThreadEnforcer.ANY);

    private static boolean register_status = false;
    private static boolean sToa_bus_reg_status = false;

    public static Bus getServiceToActivityBusInstance() {
        return serviceToActivity_bus;
    }

    public static Bus getActivityToServiceBusInstance() {
        return activityToService_bus;
    }

    public static void setRegStatus(boolean value) {
        register_status = value;
    }

    public static boolean getRegStatus() {
        return register_status;
    }

    public static void setStoaBusRegStatus(boolean value) {
        sToa_bus_reg_status = value;
    }

    public static boolean getStoaBusRegStatus() {
        return sToa_bus_reg_status;
    }
}
