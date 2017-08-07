package com.catchmind.catchmind;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDatabaseOpenHelper extends SQLiteOpenHelper
{

    public MyDatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS friendList(friendId TEXT NOT NULL PRIMARY KEY,nickname TEXT NOT NULL,profileImage TEXT,message TEXT,bookmark INTEGER);";
        try
        {
            db.execSQL(sql);
        }
        catch (SQLException e)
        {
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }

    public void createTable() {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "CREATE TABLE IF NOT EXISTS friendList(friendId TEXT NOT NULL PRIMARY KEY,nickname TEXT NOT NULL,profileImage TEXT,message TEXT,bookmark INTEGER);";
        try
        {
            db.execSQL(sql);
        }
        catch (SQLException e)
        {
        }
    }

    public void recreate() {

        SQLiteDatabase db = getWritableDatabase();

        String sql = "CREATE TABLE IF NOT EXISTS friendList(friendId TEXT NOT NULL PRIMARY KEY,nickname TEXT NOT NULL,profileImage TEXT,message TEXT,bookmark INTEGER);";
        try
        {
            db.execSQL(sql);
        }
        catch (SQLException e)
        {
        }
    }

    public void insert(String friendId, String nickname, String profile_image,String message,int bookmark) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        String sql="INSERT INTO friendList VALUES('"+friendId+"','"+nickname+"','"+profile_image +"','"+message+"','"+bookmark+"');";
        try
        {
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            db.endTransaction();
        }
        db.close();
    }


    public void clearFriendList(){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        String sql="DROP TABLE IF EXISTS friendList";
//        String sql="DROP DATABASE catchMind";
        try
        {
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            db.endTransaction();
        }
        db.close();
    }

    public void createMessageData(String userId){

        SQLiteDatabase db = getWritableDatabase();
        String sql = "CREATE TABLE IF NOT EXISTS messageData_"+userId+"(idx INTEGER PRIMARY KEY AUTOINCREMENT,no INTEGER NOT NULL,friendId TEXT NOT NULL,content TEXT,time INTEGER,type INTEGER);";
        try {
            db.execSQL(sql);
            Log.d("혹여1",sql);
        }
        catch (SQLException e) {
            Log.d("혹여2",sql);
        }

    }

    public void insertMessageData(String userId,int no,String friendId, String content,long time,int type,boolean isBounded) {
        Log.d("db.insertMD",userId+"####"+friendId+"####"+content);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        String sql="INSERT INTO messageData_"+userId+" (no,friendId,content,time,type) VALUES('"+no+"','"+friendId+"','"+content+"','"+time+"','"+type+"');";
        String sql_2;
        if(isBounded) {
            sql_2 = "UPDATE chatRoomList_" + userId + " SET `unRead`=0 WHERE no='" + no + "' AND friendId='" + friendId + "'";
        }else{
            sql_2 = "UPDATE chatRoomList_" + userId + " SET `unRead`=(`unRead`+1) WHERE no='" + no + "' AND friendId='" + friendId + "'";
        }
        Log.d("insert안",sql);
        try
        {
            db.execSQL(sql);
            db.execSQL(sql_2);
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            Log.d("db.insert","catchExecption"+userId+"####"+friendId);
            e.printStackTrace();
        }
        finally
        {
            db.endTransaction();
        }
        db.close();
    }

    public Cursor getMessageList(String userId){

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM messageData_"+userId;
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;

    }

    public Cursor getMessageListJoinChatFriendList(String userId,String friendId){

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM messageData_"+userId+" INNER JOIN chatFriendList_"+userId+" ON messageData_"+userId+".friendId = chatFriendList_"+userId+".friendId where messageData_"+userId+".friendId='"+friendId+"'";
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;

    }

    public void createMessageGroupData(String userId){
        SQLiteDatabase db = getWritableDatabase();
        String sql = "CREATE TABLE IF NOT EXISTS messageGroupData_"+userId+"(friendId TEXT NOT NULL,nickname TEXT NOT NULL,profileImage TEXT,message TEXT,dateTime TEXT,type INTEGER);";
        try
        {
            db.execSQL(sql);
        }
        catch (SQLException e)
        {
        }
    }

    public void deleteByUserId(String UserId){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        String sql="DELETE FROM friendList where friendId='"+UserId+"';";

        try
        {
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            db.endTransaction();
        }
        db.close();
    }


    public void addBookmark(String UserId){

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        String sql="UPDATE friendList SET bookmark='1' WHERE friendId='"+UserId+"';";

        try
        {
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            db.endTransaction();
        }
        db.close();

    }

    public void removeBookmark(String UserId){

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        String sql="UPDATE friendList SET bookmark='0' WHERE friendId='"+UserId+"';";

        try
        {
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            db.endTransaction();
        }
        db.close();

    }

    public Cursor getList(){

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM friendList";
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;
    }

    public Cursor getFriendData(String friendId){
        Log.d("db.getFD",friendId);
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM friendList where friendId='"+friendId+"'";
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;
    }

    public void createChatFriendList(String userId){

        SQLiteDatabase db = getWritableDatabase();
        String sql = "CREATE TABLE IF NOT EXISTS chatFriendList_"+userId+"(no INTEGER NOT NULL,friendId TEXT NOT NULL,nickname TEXT NOT NULL,profileImage TEXT,message TEXT,time INTEGER,PRIMARY KEY (no,friendId) );";
        try {
            db.execSQL(sql);
        }
        catch (SQLException e) {
            Log.d("db.exeptionCFL",userId);
        }

    }

    public void insertChatFriendData(String userId,int no,String friendId, String nickname, String profile_image,String message,long time) {
        Log.d("db.insertCFD",userId+"####"+friendId+"####"+nickname);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        String sql="INSERT INTO chatFriendList_"+userId+" VALUES('"+no+"','"+friendId+"','"+nickname+"','"+profile_image+"','"+message+"','"+time+"');";
        Log.d("insertChatFriend안",sql);
        try
        {
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            db.endTransaction();
        }
        db.close();
    }

    public void createChatRoomList(String userId){

        SQLiteDatabase db = getWritableDatabase();
        String sql = "CREATE TABLE IF NOT EXISTS chatRoomList_"+userId+"(no INTEGER NOT NULL,friendId TEXT NOT NULL,unRead INTEGER,PRIMARY KEY (no,friendId) );";
        try {
            db.execSQL(sql);
        }
        catch (SQLException e) {
            Log.d("db.exeptionCRL",userId);
        }

    }

    public void insertChatRoomData(String userId,int no,String friendId) {
        Log.d("db.insertCRD",userId+"####"+friendId+"####"+no);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        String sql="INSERT INTO chatRoomList_"+userId+" VALUES('"+no+"','"+friendId+"','0');";
        Log.d("insertChatRoom안",sql);
        try
        {
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            db.endTransaction();
        }
        db.close();
    }

    public void initailizeChatRoomUnRead(String userId,int no,String friendId) {
        Log.d("db.initICRUR",userId+"####"+friendId+"####"+no);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        String sql = "UPDATE chatRoomList_" + userId + " SET `unRead`=0 WHERE no='" + no + "' AND friendId='" + friendId + "'";
        Log.d("initICRUR",sql);
        try
        {
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            db.endTransaction();
        }
        db.close();
    }



    public Cursor getChatFriendList(String userId){
        Log.d("db.getCFL",userId);
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM chatFriendList_"+userId;
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;
    }



    public Cursor getChatRoomList(String userId){
        Log.d("db.getCRL",userId);
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM chatRoomList_"+userId;
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;
    }

    public Cursor getChatRoomListJoinChatFriendList(String userId){
        Log.d("db.getCRLJCFL",userId);
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM chatRoomList_"+userId+" INNER JOIN chatFriendList_"+userId+" ON chatRoomList_"+userId+".friendId = chatFriendList_"+userId+".friendId";
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;

    }

    public Cursor getLastRowJoinOnChatRoomList(String userId,String friendId,int no){
        Log.d("db.getLR",friendId);
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM messageData_"+userId+" INNER JOIN chatRoomList_"+userId+" ON messageData_"+userId+".no = chatRoomList_"+userId+".no AND messageData_"+userId+".friendId = chatRoomList_"+userId+".friendId WHERE messageData_"+userId+".friendId='"+friendId+"' ORDER BY messageData_"+userId+".idx DESC LIMIT 1;";
        Log.d("getLastRow",sql);
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;
    }



}

