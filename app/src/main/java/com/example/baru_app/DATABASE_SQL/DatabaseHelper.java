package com.example.baru_app.DATABASE_SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.baru_app.DATABASE_SQL.BarangayUserModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String ID = "ID";
    public static final String TABLE_NAME = "USERS";
    public static final String USER_FIRBASE_ID = "USER_ID";
    public static final String BARANGAY = "BARANGAY";
    public DatabaseHelper(Context context) {
        super(context, "TABLE_NAME", null, 1);

    }



    //    DatabaseHelper (Conte)
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + USER_FIRBASE_ID + " TEXT, " + BARANGAY + " TEXT)";

        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }

    public boolean addOneUser(BarangayUserModel model){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(USER_FIRBASE_ID,model.getUser_id());
        cv.put(BARANGAY,model.getBarangay());


        long insert = db.insert(TABLE_NAME, null, cv);
        if(insert == -1){
            return false;
        }
        else{
            return true;
        }

    }
    public String getBarangayCurrentUser(String userId){
        String resultBarangay = null;
//        boolean checker = true;
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(queryString,null);
        if(cursor.moveToFirst()) {
            while (true){
                if(cursor.getString(1).equals(userId)){
                    resultBarangay= cursor.getString(2);
                    break;
                }
            }
        }
        String returnString = resultBarangay;
        return returnString;
    }
}
