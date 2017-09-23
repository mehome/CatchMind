package com.catchmind.catchmind;



public class ChatRoomItem{


    public int No;
    public String FriendId;
    public long recentMessageTime;

    public int MemberNum;


    public ChatRoomItem(int no,String friendId,long RMT){

        this.No = no;
        this.FriendId = friendId;
        this.MemberNum = 0;
        this.recentMessageTime = RMT;
        if(no != 0){
            this.FriendId = "그룹채팅 " + no;
        }

    }



    public int getMemberNum() {
        return this.MemberNum;
    }


    public String getFriendId() {
        return this.FriendId;
    }


    public int getNo(){return this.No;}



}
