package com.ammatti.stanley.sipdialer.events.responses;

import com.ammatti.stanley.sipdialer.events.Event;

/**
 * Created by user on 25.08.15.
 */
public class ShowScreenIncomingResponse extends Event{

    private String caller_number=null;

    public ShowScreenIncomingResponse(String name) {
        super(name);
    }

    public void setCallerNumber(String number){
        this.caller_number = number;
    }

    public String getCallerNumber(){
        return this.caller_number;
    }
}
