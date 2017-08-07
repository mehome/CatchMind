package com.catchmind.catchmind;

/**
 * Created by sonsch94 on 2017-08-08.
 */

public class ListViewItemCheck {

    public String profileTxt ;
    public String nameTxt;
    public String idTxt;

    public ListViewItemCheck(String id,String name,String profile){

        this.idTxt = id;
        this.nameTxt = name;
        this.profileTxt = profile;

    }


    public void setProfile(String profile) { this.profileTxt = profile; }
    public void setId(String id){ this.idTxt = id; }
    public void setName(String name) {
        this.nameTxt = name ;
    }

    public String getId() { return this.idTxt; }
    public String getProfile() {
        return this.profileTxt ;
    }
    public String getName() {
        return this.nameTxt ;
    }

}
