package com.catchmind.catchmind;



public class ChatRoomItem{

    public String Title;
    public String Content;
    public int MemberNum;
    public String Date;

    public ChatRoomItem(String title, String content, int memberNum, String date){

        this.Title = title;
        this.Content = content;
        this.MemberNum = memberNum;
        this.Date =  date;

    }


    public void setTitle(String title) {
        this.Title = title;
    }
    public void setContent(String content) {
        this.Content = content ;
    }

    public void setMemberNum(int memberNum) {
        this.MemberNum = memberNum;
    }
    public void setDate(String date) {
        this.Date = date ;
    }

    public String getTitle() {
        return this.Title;
    }

    public String getContent() {
        return this.Content;
    }

    public int getMemberNum() {
        return this.MemberNum;
    }

    public String getDate() {
        return this.Date;
    }

}
