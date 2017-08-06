package com.catchmind.catchmind;



public class ChatRoomItem{


    public int No;
    public String FriendId;
    public String Title;
    public String Content;
    public String Profile;
    public int MemberNum;


    public ChatRoomItem(int no,String friendId,String title, int memberNum,String profile){

        this.No = no;
        this.FriendId = friendId;
        this.Title = title;
        this.MemberNum = memberNum;
        this.Profile = profile;

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


    public String getTitle() {
        return this.Title;
    }

    public String getContent() {
        return this.Content;
    }

    public int getMemberNum() {
        return this.MemberNum;
    }


    public String getFriendId() {
        return this.FriendId;
    }

    public String getProfile() {
        return this.Profile;
    }

    public int getNo(){return this.No;}


}
