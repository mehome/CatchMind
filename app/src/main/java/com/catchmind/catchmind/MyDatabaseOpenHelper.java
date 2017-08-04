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
        String sql = "CREATE TABLE IF NOT EXISTS messageData_"+userId+"(friendId TEXT NOT NULL,content TEXT,time TEXT,type INTEGER);";
        try {
            db.execSQL(sql);
        }
        catch (SQLException e) {
        }

    }

    public void insertMessageData(String userId,String friendId, String content,String time,int type) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        String sql="INSERT INTO messageData_"+userId+" VALUES('"+friendId+"','"+content+"','"+time+"','"+type+"');";
        Log.d("insert안",sql);
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

    public Cursor getMessageList(String userId){

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM messageData_"+userId;
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;

    }

    public Cursor getMessageListJoinFriendList(String userId,String friendId){

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM messageData_"+userId+" INNER JOIN friendList ON messageData_"+userId+".friendId = friendList.friendId where messageData_"+userId+".friendId='"+friendId+"'";
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
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM friendList where friendId='"+friendId+"'";
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;
    }

}

