package com.example.cp670_multilingual_ocr;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;

public class NoteDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "NoteDatabaseHelper";
    private Context ctx;
    private static final String DATABASE_NAME = "Notes.db";
    private static final Integer VERSION_NUM = 1;
    public static final String TABLE_NAME = "Notes";
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_NOTE = "note";
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME + " ( " + KEY_ID
            + " integer primary key autoincrement, " + KEY_TITLE
            + " text not null, " + KEY_NOTE
            + " text not null);";

    public NoteDatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
        this.ctx = ctx.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "inside onCreate");
        db.execSQL(DATABASE_CREATE);

        // -- Insert test notes:

        String[] titles = this.ctx.getResources().getStringArray(R.array.test_notes_titles);
        String[] notes = this.ctx.getResources().getStringArray(R.array.test_notes_notes);

        StringBuilder strBuilder = new StringBuilder("insert into " + TABLE_NAME + " (" + KEY_TITLE + ", " + KEY_NOTE + ") ");
        strBuilder.append("values ");
        for(int i = 0; i < titles.length; i++){
            strBuilder.append("('").append(titles[i]).append("', '").append(notes[i]).append("'),");
        }
        strBuilder.replace(strBuilder.length() - 1, strBuilder.length(), ";");
        String insertTestNotes = strBuilder.toString();

        db.execSQL(insertTestNotes);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Calling onUpgrade, oldVersion=" + oldVersion + " newVersion=" + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
