package com.ammatti.stanley.sipdialer.events.others;

import com.ammatti.stanley.sipdialer.events.Event;

import org.linphone.mediastream.video.AndroidVideoWindowImpl;

/**
 * Created by 1310042 on 2016/1/26.
 */
public class AndroidVideoWindowImplEvent extends Event{

    private AndroidVideoWindowImpl currentvideowindow = null;

    public AndroidVideoWindowImplEvent(String name) {
        super(name);
    }

    public void setWindow(AndroidVideoWindowImpl window){
        this.currentvideowindow = window;
    }

    public AndroidVideoWindowImpl getWindow(){
        return this.currentvideowindow;
    }
}
