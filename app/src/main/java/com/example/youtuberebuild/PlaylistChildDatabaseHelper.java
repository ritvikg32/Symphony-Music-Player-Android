package com.example.youtuberebuild;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class PlaylistChildDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "mylist2.db";
    public static final String TABLE_NAME = "mylist_data";
    public static final String COL1 = "ID";
    public static final String COL3 = "PID";
    public static final String COL2 = "ITEM1";
    public static final String COL4 = "NAME";



    public PlaylistChildDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " ITEM1 TEXT, " + "PID  INTEGER, " + " NAME TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(byte item1[], int pId, String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2,item1);
        contentValues.put(COL3,pId);
        contentValues.put(COL4,name);

       // contentValues.put(COL2,item1);

        long result = db.insert(TABLE_NAME,null,contentValues);

        if(result==-1){
            return false;
        }
        else{
            return true;
        }
    }
    public Cursor getListContents(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return data;
    }

    public Cursor getItemID(int pId,String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL1 + " FROM " + TABLE_NAME +
                " WHERE " + COL4 + " = '" + name + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }


    /**
     * Delete from database
     * @param id
     *
     */
    public void deleteName(int id,String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE "
                + COL1 + " = '" + id + "'"+
                " AND " + COL4 + " = '" + name + "'";
        Log.d(TAG, "deleteName: query: " + query);
        db.execSQL(query);
    }

}
