package com.catchmind.catchmind;

import android.graphics.drawable.Drawable;


public class ListViewItem {
    public String profileTxt ;
    public String nameTxt;
    public String messageTxt;
    public String idTxt;

    public ListViewItem(String id,String name,String profile, String message){

        this.idTxt = id;
        this.nameTxt = name;
        this.messageTxt = message;
        this.profileTxt = profile;

    }


    public void setProfile(String profile) { this.profileTxt = profile; }
    public void setId(String id){ this.idTxt = id; }
    public void setName(String name) {
        this.nameTxt = name ;
    }
    public void setMessage(String message) {
        this.messageTxt = message ;
    }

    public String getId() { return this.idTxt; }
    public String getProfile() {
        return this.profileTxt ;
    }
    public String getName() {
        return this.nameTxt ;
    }
    public String getMessage() {
        return this.messageTxt ;
    }
}

