package com.catchmind.catchmind;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public void insertMessageData(String userId,int no,String friendId, String content,long time,int type) {
        Log.d("db.insertMD",userId+"####"+friendId+"####"+content);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        String sql="INSERT INTO messageData_"+userId+" (no,friendId,content,time,type) VALUES('"+no+"','"+friendId+"','"+content+"','"+time+"','"+type+"');";

        Log.d("insert안",sql);
        try
        {
            db.execSQL(sql);
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

    public Cursor getMessageListJoinChatFriendList(String userId,String friendId,int no){

        SQLiteDatabase db = this.getReadableDatabase();
        String sql;
        if(no==0) {
            sql = "SELECT * FROM messageData_" + userId + " INNER JOIN chatFriendList_" + userId + " ON messageData_" + userId + ".friendId = chatFriendList_" + userId + ".friendId WHERE messageData_" + userId + ".friendId='" + friendId + "' AND messageData_"+userId+".no='0'" ;
        }else{
            sql = "SELECT * FROM messageData_" + userId + " INNER JOIN chatFriendList_" + userId + " ON messageData_" + userId + ".friendId = chatFriendList_" + userId + ".friendId AND messageData_" + userId + ".no = chatFriendList_" + userId + ".no WHERE messageData_" + userId + ".no='" + no +"'" ;
        }
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;

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
        String sql_del="DROP TABLE IF EXISTS chatFriendList_"+userId;
        String sql = "CREATE TABLE IF NOT EXISTS chatFriendList_"+userId+"(no INTEGER NOT NULL,friendId TEXT NOT NULL,nickname TEXT NOT NULL,profileImage TEXT,message TEXT,time INTEGER,PRIMARY KEY (no,friendId) );";
        try {
            db.execSQL(sql_del);
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

    public void insertChatFriendDataMultipleByJoin(String userId, String friendId, int no){
        try {
            JSONArray jarray = new JSONArray(friendId);
            String sql = "SELECT * FROM friendList WHERE friendId='"+jarray.getString(0)+"'";
            Log.d("ICFDMBJ",sql);
            for(int i=1; i<jarray.length();i++){
                sql = sql + " OR friendId='"+jarray.getString(i)+"'";
            }

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(sql,null);

            db.beginTransaction();

            String sql_2="INSERT INTO chatFriendList_"+userId+" VALUES ";

            cursor.moveToNext();

            sql_2 = sql_2 + "('"+no+"','"+cursor.getString(0)+"','"+cursor.getString(1)+"','"+cursor.getString(2)+"','"+cursor.getString(3)+"','0')";
            while(cursor.moveToNext()){
                sql_2 = sql_2 + ",('"+no+"','"+cursor.getString(0)+"','"+cursor.getString(1)+"','"+cursor.getString(2)+"','"+cursor.getString(3)+"','0')";
            }

            Log.d("ICFDM",sql_2);

            try
            {
                db.execSQL(sql_2);
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

        }catch(JSONException e){
            Log.d("ICFDM.JSONException",friendId);
            e.printStackTrace();

        }
    }


    public void insertChatFriendDataMultiple(String userId, String jarray){
        try {
            SQLiteDatabase db = getWritableDatabase();
            JSONArray chatArray = new JSONArray(jarray);

            String sql="INSERT INTO chatFriendList_"+userId+" VALUES ";

            for(int i=0;i<chatArray.length();i++){
                if(i>0){
                    sql = sql + ",";
                }
                JSONObject jobject = new JSONObject(chatArray.get(i).toString());
                int no = (int) jobject.getInt("no");
                String friendId = (String) jobject.getString("friendId");
                String nickname = (String) jobject.getString("nickname");
                String profile = (String) jobject.getString("profile");
                String message = (String) jobject.getString("message");
                long time = (long) jobject.getLong("time");

                sql = sql + "('"+no+"','"+friendId+"','"+nickname+"','"+profile+"','"+message+"','"+time+"')";
            }


            Log.d("ICFDM",sql);

            db.beginTransaction();

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

        }catch(JSONException e){
            Log.d("ICFDM.JSONException",jarray);
            e.printStackTrace();

        }
    }

    public void createChatRoomList(String userId){

        SQLiteDatabase db = getWritableDatabase();
        String sql_del="DROP TABLE IF EXISTS chatRoomList_"+userId;
        String sql = "CREATE TABLE IF NOT EXISTS chatRoomList_"+userId+"(no INTEGER NOT NULL,friendId TEXT NOT NULL,time INTEGER,PRIMARY KEY (no,friendId) );";
        try {
            db.execSQL(sql_del);
            db.execSQL(sql);
        }
        catch (SQLException e) {
            Log.d("db.exeptionCRL",userId);
        }

    }

    public void insertChatRoomData(String userId,int no,String friendId,long time) {
        Log.d("db.insertCRD",userId+"####"+friendId+"####"+no);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        String sql="INSERT INTO chatRoomList_"+userId+" VALUES('"+no+"','"+friendId+"','"+time+"');";
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


    public void updateChatRoomData(String userId,int no,String friendId,long time) {
        Log.d("db.insertCRD",userId+"####"+friendId+"####"+no);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        String sql;
        if(no == 0){
            sql = "UPDATE chatRoomList_" + userId + " SET time='" + time + "' WHERE friendId='"+friendId+"' AND no='"+no+"';";
        }else {
            sql = "UPDATE chatRoomList_" + userId + " SET time='" + time + "' WHERE no='"+no+"';";
        }
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

//    public void initailizeChatRoomUnRead(String userId,int no,String friendId) {
//        Log.d("db.initICRUR",userId+"####"+friendId+"####"+no);
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.beginTransaction();
//        String sql = "UPDATE chatRoomList_" + userId + " SET `unRead`=0 WHERE no='" + no + "' AND friendId='" + friendId + "'";
//        Log.d("initICRUR",sql);
//        try
//        {
//            db.execSQL(sql);
//            db.setTransactionSuccessful();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            db.endTransaction();
//        }
//        db.close();
//    }



    public Cursor getChatFriendList(String userId){
        Log.d("db.getCFL",userId);
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM chatFriendList_"+userId;
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;
    }

    public Cursor getChatFriendListByNo(String userId,int no){
        Log.d("db.getCFLByNO",userId);
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM chatFriendList_"+userId+" WHERE no='"+no+"'";
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;
    }

    public Cursor getChatFriendListByIdAndNo(String userId,int no,String friendId){
        Log.d("db.getCFLByIdNo",userId);
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM chatFriendList_"+userId+" WHERE no='"+no+"' AND friendId='"+friendId+"'";
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
        String sql;
        if(no == 0) {
            sql = "SELECT * FROM messageData_" + userId + " INNER JOIN chatRoomList_" + userId + " ON messageData_" + userId + ".no = chatRoomList_" + userId + ".no AND messageData_" + userId + ".friendId = chatRoomList_" + userId + ".friendId WHERE messageData_" + userId + ".friendId='" + friendId + "' AND messageData_"+userId+".no='"+no+"' ORDER BY messageData_" + userId + ".idx DESC LIMIT 1;";
        }else{
            sql = "SELECT * FROM messageData_" + userId + " INNER JOIN chatRoomList_" + userId + " ON messageData_" + userId + ".no = chatRoomList_" + userId + ".no WHERE messageData_" + userId + ".no='" + no + "' ORDER BY messageData_" + userId + ".idx DESC LIMIT 1;";
        }
        Log.d("getLastRow",sql);
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;
    }

    public int getMinNo(String userId){
        Log.d("db.getMinNO","getMinNO###"+userId);
        SQLiteDatabase db = this.getReadableDatabase();
        String sql;

        sql = "SELECT * FROM chatRoomList_" + userId + " ORDER BY no ASC LIMIT 1;";

        Log.d("getMinNo",sql);
        Cursor cursor = db.rawQuery(sql,null);
        while(cursor.moveToNext()) {
            int result = cursor.getInt(0) - 1;
            return result;
        }

            return -1;
    }

    public int getUnRead(String userId,String friendId, int no ,long cTime){

        SQLiteDatabase db = this.getReadableDatabase();
        String sql;
        if(no ==0) {
            sql = "SELECT COUNT(*) FROM messageData_" + userId + " WHERE no='0' AND friendId='"+friendId+"' AND time >"+cTime+" AND type='1'";
        }else{
            sql = "SELECT COUNT(*) FROM messageData_" + userId + " WHERE no='"+no+"' AND time >"+cTime+" AND type='1'";
        }
        Log.d("getLastRow",sql);
        Cursor cursor = db.rawQuery(sql,null);
        cursor.moveToNext();
        int result = cursor.getInt(0) ;

        return result;

    }


}

