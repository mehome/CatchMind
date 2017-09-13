package com.catchmind.catchmind;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class MyDatabaseOpenHelper extends SQLiteOpenHelper
{

    SQLiteDatabase dbr;
    SQLiteDatabase dbw;
    public SharedPreferences mPref;


    public MyDatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        dbr = getReadableDatabase();
        dbw = getWritableDatabase();
        mPref = context.getSharedPreferences("login",MODE_PRIVATE);
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
//        dbw = getWritableDatabase();
        String sql = "CREATE TABLE IF NOT EXISTS friendList(friendId TEXT NOT NULL PRIMARY KEY,nickname TEXT NOT NULL,profileImage TEXT,message TEXT,bookmark INTEGER);";
        try
        {
            dbw.execSQL(sql);
        }
        catch (SQLException e)
        {
        }
    }

    public void recreate() {

//        SQLiteDatabase db = getWritableDatabase();

        String sql = "CREATE TABLE IF NOT EXISTS friendList(friendId TEXT NOT NULL PRIMARY KEY,nickname TEXT NOT NULL,profileImage TEXT,message TEXT,bookmark INTEGER);";
        try
        {
            dbw.execSQL(sql);
        }
        catch (SQLException e)
        {
        }
    }

    public void insert(String friendId, String nickname, String profile_image,String message,int bookmark) {
//        SQLiteDatabase db = this.getWritableDatabase();
        dbw.beginTransaction();
        String sql="INSERT INTO friendList VALUES('"+friendId+"','"+nickname+"','"+profile_image +"','"+message+"','"+bookmark+"');";
        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }
//        db.close();
    }


    public void clearFriendList(){
//        SQLiteDatabase db = getWritableDatabase();
        dbw.beginTransaction();
        String sql="DROP TABLE IF EXISTS friendList";
//        String sql="DROP DATABASE catchMind";
        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }
//        db.close();
    }

    public void createMessageData(String userId){

//        SQLiteDatabase db = getWritableDatabase();
        String sql = "CREATE TABLE IF NOT EXISTS messageData_"+userId+"(idx INTEGER PRIMARY KEY AUTOINCREMENT,no INTEGER NOT NULL,friendId TEXT NOT NULL,content TEXT,time INTEGER,type INTEGER);";
        try {
            dbw.execSQL(sql);
            Log.d("혹여1",sql);
        }
        catch (SQLException e) {
            Log.d("혹여2",sql);
        }

    }

    public void insertMessageData(String userId,int no,String friendId, String content,long time,int type) {
        Log.d("db.insertMD",userId+"####"+friendId+"####"+content);
//        SQLiteDatabase db = this.getWritableDatabase();
        dbw.beginTransaction();
        content = content.replace("'","''");

        String sql="INSERT INTO messageData_"+userId+" (no,friendId,content,time,type) VALUES('"+no+"','"+friendId+"','"+content+"','"+time+"','"+type+"');";

        Log.d("insert안",sql);
        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            Log.d("db.insert","catchExecption"+userId+"####"+friendId);
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }
//        db.close();
    }

    public Cursor getMessageList(String userId){

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM messageData_"+userId;
        Cursor cursor = db.rawQuery(sql,null);

        return cursor;

    }

    public Cursor getMessageListJoinChatFriendList(String userId,String friendId,int no){

//        SQLiteDatabase db = this.getReadableDatabase();
        String sql;
        if(no==0) {
            sql = "SELECT * FROM messageData_" + userId + " INNER JOIN chatFriendList ON messageData_" + userId + ".friendId = chatFriendList.friendId AND messageData_" + userId + ".no = chatFriendList.no WHERE messageData_" + userId + ".friendId='" + friendId + "' AND messageData_"+userId+".no='0'" ;
        }else{
            sql = "SELECT * FROM messageData_" + userId + " INNER JOIN chatFriendList ON messageData_" + userId + ".friendId = chatFriendList.friendId AND messageData_" + userId + ".no = chatFriendList.no WHERE messageData_" + userId + ".no='" + no +"'" ;
        }
        Cursor cursor = dbr.rawQuery(sql,null);

        return cursor;

    }



    public void deleteByUserId(String UserId){
//        SQLiteDatabase db = getWritableDatabase();
        dbw.beginTransaction();
        String sql="DELETE FROM friendList where friendId='"+UserId+"';";

        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }
//        db.close();
    }


    public void addBookmark(String UserId){

//        SQLiteDatabase db = getWritableDatabase();
        dbw.beginTransaction();
        String sql="UPDATE friendList SET bookmark='1' WHERE friendId='"+UserId+"';";

        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }
//        db.close();

    }

    public void removeBookmark(String UserId){

//        SQLiteDatabase db = getWritableDatabase();
        dbw.beginTransaction();
        String sql="UPDATE friendList SET bookmark='0' WHERE friendId='"+UserId+"';";

        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }
//        db.close();

    }

    public Cursor getList(){

//        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM friendList";
        Cursor cursor = dbr.rawQuery(sql,null);

        return cursor;
    }


    public Cursor getFriendData(String friendId){
        Log.d("db.getFD",friendId);
//        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM friendList where friendId='"+friendId+"'";
        Cursor cursor = dbr.rawQuery(sql,null);

        return cursor;
    }

    public Cursor getChatFriendData(String friendId){

        Log.d("db.getCFD",friendId);
//        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM chatFriendList where friendId='"+friendId+"' AND no='0'";
        Cursor cursor = dbr.rawQuery(sql,null);

        return cursor;
    }

    public void createChatFriendList(){

//        SQLiteDatabase db = getWritableDatabase();
        String sql_del="DROP TABLE IF EXISTS chatFriendList";
        String sql = "CREATE TABLE IF NOT EXISTS chatFriendList(no INTEGER NOT NULL,friendId TEXT NOT NULL,nickname TEXT NOT NULL,profileImage TEXT,message TEXT,time INTEGER,PRIMARY KEY (no,friendId) );";
        try {
            dbw.execSQL(sql_del);
            dbw.execSQL(sql);
        }
        catch (SQLException e) {
            Log.d("db.exeptionCFL",sql);
        }

    }

    public void insertChatFriendData(int no,String friendId, String nickname, String profile_image,String message,long time) {
        Log.d("db.insertCFD","####"+friendId+"####"+nickname);
//        SQLiteDatabase db = this.getWritableDatabase();
        dbw.beginTransaction();
        String sql="INSERT INTO chatFriendList VALUES('"+no+"','"+friendId+"','"+nickname+"','"+profile_image+"','"+message+"','"+time+"');";
        Log.d("insertChatFriend안",sql);
        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }
//        db.close();
    }

    public void insertChatFriendDataMultipleByJoin(String friendId, int no){
        try {
            JSONArray jarray = new JSONArray(friendId);
            String sql = "SELECT * FROM friendList WHERE friendId='"+jarray.getString(0)+"'";
            Log.d("ICFDMBJ",sql);
            for(int i=1; i<jarray.length();i++){
                sql = sql + " OR friendId='"+jarray.getString(i)+"'";
            }

//            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = dbr.rawQuery(sql,null);

            dbr.beginTransaction();

            String sql_2="INSERT INTO chatFriendList VALUES ";

            cursor.moveToNext();

            sql_2 = sql_2 + "('"+no+"','"+cursor.getString(0)+"','"+cursor.getString(1)+"','"+cursor.getString(2)+"','"+cursor.getString(3)+"','0')";
            while(cursor.moveToNext()){
                sql_2 = sql_2 + ",('"+no+"','"+cursor.getString(0)+"','"+cursor.getString(1)+"','"+cursor.getString(2)+"','"+cursor.getString(3)+"','0')";
            }
            sql_2 = sql_2 + ",('"+no+"','"+mPref.getString("userId","null")+"','"+mPref.getString("nickname","null")+"','"+mPref.getString("profile","null")+"','"+mPref.getString("message","null")+"','0')";

            Log.d("ICFDM",sql_2);

            try
            {
                dbr.execSQL(sql_2);
                dbr.setTransactionSuccessful();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                dbr.endTransaction();
            }
//            db.close();

        }catch(JSONException e){
            Log.d("ICFDM.JSONException",friendId);
            e.printStackTrace();

        }
    }


    public void insertChatFriendDataMultipleByJoinInvite(String friendId, int no){
        try {
            JSONArray jarray = new JSONArray(friendId);
            String sql = "SELECT * FROM friendList WHERE friendId='"+jarray.getString(0)+"'";
            Log.d("ICFDMBJI",sql);
            for(int i=1; i<jarray.length();i++){
                sql = sql + " OR friendId='"+jarray.getString(i)+"'";
            }

//            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = dbr.rawQuery(sql,null);

            dbr.beginTransaction();

            String sql_2="INSERT INTO chatFriendList VALUES ";

            cursor.moveToNext();

            sql_2 = sql_2 + "('"+no+"','"+cursor.getString(0)+"','"+cursor.getString(1)+"','"+cursor.getString(2)+"','"+cursor.getString(3)+"','0')";
            while(cursor.moveToNext()){
                sql_2 = sql_2 + ",('"+no+"','"+cursor.getString(0)+"','"+cursor.getString(1)+"','"+cursor.getString(2)+"','"+cursor.getString(3)+"','0')";
            }

            Log.d("ICFDMI",sql_2);

            try
            {
                dbr.execSQL(sql_2);
                dbr.setTransactionSuccessful();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                dbr.endTransaction();
            }
//            db.close();

        }catch(JSONException e){
            Log.d("ICFDM.JSONException",friendId);
            e.printStackTrace();

        }
    }



    public void insertChatFriendDataMultiple(String jarray){
        try {
//            SQLiteDatabase db = getWritableDatabase();
            JSONArray chatArray = new JSONArray(jarray);

            String sql="INSERT INTO chatFriendList VALUES ";

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

            dbw.beginTransaction();

            try
            {
                dbw.execSQL(sql);
                dbw.setTransactionSuccessful();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                dbw.endTransaction();
            }
//            db.close();

        }catch(JSONException e){
            Log.d("ICFDM.JSONException",jarray);
            e.printStackTrace();

        }
    }

    public void createChatRoomList(){

//        SQLiteDatabase db = getWritableDatabase();
        String sql_del="DROP TABLE IF EXISTS chatRoomList";
        String sql = "CREATE TABLE IF NOT EXISTS chatRoomList(no INTEGER NOT NULL,friendId TEXT NOT NULL,time INTEGER,PRIMARY KEY (no,friendId) );";
        try {
            dbw.execSQL(sql_del);
            dbw.execSQL(sql);
        }
        catch (SQLException e) {
            Log.d("db.exeptionCRL","noUserId");
        }

    }

    public void insertChatRoomData(int no,String friendId,long time) {
        Log.d("db.insertCRD","####"+friendId+"####"+no);
//        SQLiteDatabase db = this.getWritableDatabase();
        dbw.beginTransaction();
        String sql="INSERT INTO chatRoomList VALUES('"+no+"','"+friendId+"','"+time+"');";
        Log.d("insertChatRoom안",sql);
        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }
//        db.close();
    }


    public void updateChatRoomData(int no,String friendId,long time) {
        Log.d("db.updateCRD","####"+friendId+"####"+no);
//        SQLiteDatabase db = this.getWritableDatabase();
        dbw.beginTransaction();
        String sql;
        if(no == 0){
            sql = "UPDATE chatRoomList SET time='" + time + "' WHERE friendId='"+friendId+"' AND no='"+no+"';";
        }else {
            sql = "UPDATE chatRoomList SET time='" + time + "' WHERE no='"+no+"';";
        }
        Log.d("updateChatRD안",sql);
        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }
//        db.close();
    }



    public void updateChatFriendData(int no,String friendId,long time) {
        Log.d("db.updateCFD","####"+friendId+"####"+no);
//        SQLiteDatabase db = this.getWritableDatabase();
        dbw.beginTransaction();
        String sql = "UPDATE chatFriendList SET time='" + time + "' WHERE friendId='"+friendId+"' AND no='"+no+"';";

        Log.d("updateChatFD안",sql);
        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }
//        db.close();
    }

    public Cursor getChatFriendListDataWithMe(String friendId){
        Log.d("db.getCFDWM",friendId);
        String sql = "SELECT * FROM chatFriendList WHERE (friendId='"+friendId+"' AND no='0') OR (friendId='"+mPref.getString("userId","nono")+"' AND no='0')";
        Cursor cursor = dbr.rawQuery(sql,null);

        return cursor;
    }

    public Cursor getChatFriendList(){
        Log.d("db.getCFL","noUserId");
//        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM chatFriendList";
        Cursor cursor = dbr.rawQuery(sql,null);

        return cursor;
    }

    public Cursor getChatFriendListByNo(int no){
        Log.d("db.getCFLByNO","noUserId");
//        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM chatFriendList WHERE no='"+no+"'";
        Cursor cursor = dbr.rawQuery(sql,null);

        return cursor;
    }

    public Cursor getChatFriendListByIdAndNo(int no,String friendId){
        Log.d("db.getCFLByIdNo",friendId);
//        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM chatFriendList WHERE no='"+no+"' AND friendId='"+friendId+"'";
        Cursor cursor = dbr.rawQuery(sql,null);

        return cursor;
    }




    public Cursor getChatRoomList(){
        Log.d("db.getCRL","noUserId");
//        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM chatRoomList";
        Cursor cursor = dbr.rawQuery(sql,null);

        return cursor;
    }


    public Cursor getChatRoomListUnread(){
        Log.d("db.getCRL","noUserId");
//        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM chatRoomList ORDER BY time ASC";
        Cursor cursor = dbr.rawQuery(sql,null);

        return cursor;
    }

    public Cursor getChatRoomListJoinChatFriendList(){
        Log.d("db.getCRLJCFL","noUserId");
//        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM chatRoomList INNER JOIN chatFriendList ON chatRoomList.friendId = chatFriendList.friendId";
        Cursor cursor = dbr.rawQuery(sql,null);

        return cursor;

    }

    public Cursor getLastRowJoinOnChatRoomList(String userId,String friendId,int no){
        Log.d("db.getLR",friendId);
//        SQLiteDatabase db = this.getReadableDatabase();

        String sql;

//        if(no == 0) {
//            sql = "SELECT * FROM messageData_" + userId + " INNER JOIN chatRoomList ON messageData_" + userId + ".no = chatRoomList.no AND messageData_" + userId + ".friendId = chatRoomList.friendId WHERE messageData_" + userId + ".friendId='" + friendId + "' AND messageData_"+userId+".no='"+no+"' AND ( messageData_"+ userId +".type = '1' OR messageData_" + userId +".type = '2') ORDER BY messageData_" + userId + ".idx DESC LIMIT 1;";
//        }else{
//            sql = "SELECT * FROM messageData_" + userId + " INNER JOIN chatRoomList ON messageData_" + userId + ".no = chatRoomList.no WHERE messageData_" + userId + ".no='" + no + "' AND ( messageData_"+ userId +".type = '1' OR messageData_" + userId +".type = '2') ORDER BY messageData_" + userId + ".idx DESC LIMIT 1;";
//        }


        if(no == 0) {
            sql = "SELECT * FROM messageData_" + userId + " INNER JOIN chatRoomList ON messageData_" + userId + ".no = chatRoomList.no AND messageData_" + userId + ".friendId = chatRoomList.friendId WHERE messageData_" + userId + ".friendId='" + friendId + "' AND messageData_"+userId+".no='"+no+"' AND ( messageData_"+ userId +".type = '1' OR messageData_" + userId +".type = '2' OR messageData_" + userId +".type = '51' OR messageData_" + userId +".type = '52') ORDER BY messageData_" + userId + ".idx DESC LIMIT 1;";
        }else{
            sql = "SELECT * FROM messageData_" + userId + " INNER JOIN chatRoomList ON messageData_" + userId + ".no = chatRoomList.no WHERE messageData_" + userId + ".no='" + no + "' AND ( messageData_"+ userId +".type = '1' OR messageData_" + userId +".type = '2' OR messageData_" + userId +".type = '51' OR messageData_" + userId +".type = '52') ORDER BY messageData_" + userId + ".idx DESC LIMIT 1;";
        }


        Log.d("getLastRow",sql);
        Cursor cursor = dbr.rawQuery(sql,null);

        return cursor;
    }

    public int getMinNo(){
        Log.d("db.getMinNO","getMinNO###noUserId");
//        SQLiteDatabase db = this.getReadableDatabase();
        String sql;

        sql = "SELECT * FROM chatRoomList ORDER BY no ASC LIMIT 1;";

        Log.d("getMinNo",sql);
        Cursor cursor = dbr.rawQuery(sql,null);
        while(cursor.moveToNext()) {
            int result = cursor.getInt(0) - 1;
            return result;
        }

            return -1;
    }

    public int getUnRead(String userId,String friendId, int no ,long cTime){

//        SQLiteDatabase db = this.getReadableDatabase();
        String sql;
        if(no ==0) {
            sql = "SELECT COUNT(*) FROM messageData_" + userId + " WHERE no='0' AND friendId='"+friendId+"' AND time >"+cTime+" AND (type='1' OR type ='51')";
        }else{
            sql = "SELECT COUNT(*) FROM messageData_" + userId + " WHERE no='"+no+"' AND time >"+cTime+" AND (type='1' OR type ='51')";
        }
        Log.d("getUnRead",sql);
        Cursor cursor = dbr.rawQuery(sql,null);
        cursor.moveToNext();
        int result = cursor.getInt(0) ;

        return result;

    }

    public int getUnReadWithRight(String userId,String friendId, int no ,long cTime){

        if(no ==0) {
            return 0;
        }

//        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM chatFriendList WHERE no='"+no+"' AND friendId NOT IN ('"+userId+"','"+friendId+"') AND time <"+cTime;

        Log.d("getUnReadWith1",sql);
        Cursor cursor = dbr.rawQuery(sql,null);
        cursor.moveToNext();
        int result = cursor.getInt(0) ;

        return result;

    }


    public int getUnReadWithLeft(String userId,String friendId, int no ,long cTime){


//        SQLiteDatabase db = this.getReadableDatabase();
        String sql;
        if(no == 0){
            sql = "SELECT COUNT(*) FROM chatFriendList WHERE no='0' AND friendId = '"+friendId+"' AND time <" + cTime;
        }else {
            sql = "SELECT COUNT(*) FROM chatFriendList WHERE no='" + no + "' AND friendId NOT IN ('" + userId + "') AND time <" + cTime;
        }
        Log.d("getUnReadWith2",sql);
        Cursor cursor = dbr.rawQuery(sql,null);
        cursor.moveToNext();
        int result = cursor.getInt(0) ;

        return result;

    }


    public void deleteRoom(int no, String friendId){

        dbw.beginTransaction();
        String sql;
        if(no ==0) {
            sql = "DELETE FROM chatRoomList WHERE no='0' AND friendId='"+friendId+"'";
        }else{
            sql = "DELETE FROM chatRoomList WHERE no='" + no + "'";
        }
        Log.d("deleteRoom안",sql);
        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }

    }


    public void deleteChatFriendAll(int no, String friendId){


        dbw.beginTransaction();
        String sql;
        if(no ==0) {
            sql = "DELETE FROM chatFriendList WHERE no='0' AND friendId='"+friendId+"'";
        }else{
            sql = "DELETE FROM chatFriendList WHERE no='" + no + "'";
        }
        Log.d("deleteChatFriendAll안",sql);
        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }


    }

    public void deleteMessageData(int no, String friendId){


        dbw.beginTransaction();
        String sql;
        if(no ==0) {
            sql = "DELETE FROM messageData_"+mPref.getString("userId","noId")+" WHERE no='0' AND friendId='"+friendId+"'";
        }else{
            sql = "DELETE FROM messageData_"+mPref.getString("userId","noId")+" WHERE no='"+no+"'";
        }
        Log.d("deleteMessageData안",sql);
        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }


    }



    public void deleteChatFriend(int no, String friendId){


        dbw.beginTransaction();

        String sql = "DELETE FROM chatFriendList WHERE no='" + no + "' AND friendId='" + friendId +"'";

        Log.d("deleteChatFriend안",sql);
        try
        {
            dbw.execSQL(sql);
            dbw.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbw.endTransaction();
        }


    }


    public Cursor getChatRoomListJoinWithMessage(){

        Log.d("db.getCRLJWM","noUserId");
        String userId = mPref.getString("userId","null");
//        SQLiteDatabase db = this.getReadableDatabase();
//        String sql = "SELECT * FROM chatRoomList AS cr LEFT JOIN messageData_"+userId+" AS md ON md.friendId = (SELECT md1.friendId FROM messageData_"+userId+" AS md1 WHERE cr.no = md1.no AND cr.friendId = md1.friendId AND (md1.type = 1 OR md1.type = 2) ORDER BY md1.time DESC LIMIT 1)";
        String sql = "SELECT * FROM chatRoomList AS cr LEFT JOIN messageData_"+userId+" AS md ON CASE WHEN cr.no IN (0) THEN (md.friendId = cr.friendId AND md.no = cr.no) ELSE (md.no = cr.no) END GROUP BY cr.no,cr.friendId";

        Cursor cursor = dbr.rawQuery(sql,null);

        return cursor;

    }




}

