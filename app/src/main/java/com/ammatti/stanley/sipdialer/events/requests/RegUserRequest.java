package com.ammatti.stanley.sipdialer.events.requests;

import com.ammatti.stanley.sipdialer.events.Event;

/**
 * Created by user on 25.08.15.
 */
public class RegUserRequest extends Event{

    private String server_address = null;
    private String user_account = null;
    private String user_password = null;

    public RegUserRequest(String name) {
        super(name);
    }

    public void setServerAddress(String address){
        this.server_address=address;
    }

    public String getServerAddress(){
        return this.server_address;
    }

    public void setUserAccount(String account){
        this.user_account=account;
    }

    public String getUserAccount(){
        return this.user_account;
    }

    public void setUserPassword(String password){
        this.user_password=password;
    }

    public String getUserPassword(){
        return this.user_password;
    }
}
