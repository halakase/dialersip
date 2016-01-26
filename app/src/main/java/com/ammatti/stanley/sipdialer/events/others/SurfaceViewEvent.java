package com.ammatti.stanley.sipdialer.events.others;

import android.view.SurfaceView;

import com.ammatti.stanley.sipdialer.events.Event;

/**
 * Created by 1310042 on 2016/1/26.
 */
public class SurfaceViewEvent extends Event {

    private SurfaceView currentview = null;

    public SurfaceViewEvent(String name) {
        super(name);
    }

    public void setView(SurfaceView view){
        this.currentview = view;
    }

    public SurfaceView getView(){
        return this.currentview;
    }
}
