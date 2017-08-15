package com.catchmind.catchmind;



public class ChatRoomItem{


    public int No;
    public String FriendId;

    public int MemberNum;


    public ChatRoomItem(int no,String friendId){

        this.No = no;
        this.FriendId = friendId;
        this.MemberNum = 0;

    }





    public int getMemberNum() {
        return this.MemberNum;
    }


    public String getFriendId() {
        return this.FriendId;
    }

    public int getNo(){return this.No;}


}
