package com.ammatti.stanley.sipdialer.events.requests;

import com.ammatti.stanley.sipdialer.events.Event;

/**
 * Created by user on 25.08.15.
 */
public class MakeCallRequest extends Event{

    private String callee_number=null;

    public MakeCallRequest(String name) {
        super(name);
    }

    public void setCalleeNumber(String number){
        this.callee_number=number;
    }

    public String getCalleeNumber(){
        return this.callee_number;
    }
}
