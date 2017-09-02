package com.catchmind.catchmind;

/**
 * Created by sonsch94 on 2017-09-02.
 */

public class drawChatItem {

    String Nickname;
    String Content;


    public drawChatItem(String nickname , String content){

        this.Nickname = nickname;
        this.Content = content;

    }



    public String getNickname() {
        return this.Nickname;
    }

    public String getContent() {
        return this.Content;
    }

}
