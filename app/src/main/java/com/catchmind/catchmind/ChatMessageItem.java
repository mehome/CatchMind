package com.catchmind.catchmind;

/**
 * Created by sonsch94 on 2017-07-19.
 */

public class ChatMessageItem {

    public int Type;
    public String Nickname;
    public String ProfileImage;
    public String Content;
    public String Time;

    public ChatMessageItem(int type, String nickname, String content, String time){

        this.Type = type;
        this.Nickname = nickname;
        this.Content = content;
        this.Time =  time;

    }


    public void setType(int type) { this.Type = type; }

    public void setNickname(String nickname) { this.Nickname = nickname ; }

    public void setContent(String content) {
        this.Content = content ;
    }

    public void setTime(String time) { this.Time = time ; }



    public int getType() { return this.Type; }

    public String getNickname() { return this.Nickname; }

    public String getContent() {
        return this.Content;
    }

    public String getTime() {
        return this.Time;
    }


}
