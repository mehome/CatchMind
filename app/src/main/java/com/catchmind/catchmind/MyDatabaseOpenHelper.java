package com.catchmind.catchmind;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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


    public void clear(){
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


    public Cursor getList(){

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM friendList";
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;
    }

}

