package com.ammatti.stanley.sipdialer.events;

/**
 * Created by 1310042 on 2015/9/22.
 */
public class Event {

    private String event_name=null;

    public Event(String name){
        this.event_name=name;
    }

    public void setEventName(String name){
        this.event_name=name;
    }

    public String getEventName(){
        return this.event_name;
    }
}
