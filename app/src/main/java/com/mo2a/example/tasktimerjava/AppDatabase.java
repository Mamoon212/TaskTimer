package com.mo2a.example.tasktimerjava;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class AppDatabase extends SQLiteOpenHelper {
    private static final String TAG = "AppDatabase";
    public static final String DATABASE_NAME = "TaskTimer.db";
    public static final int DATABASE_VERSION = 3;
    private static AppDatabase instance = null;

    private AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new AppDatabase(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sSQL = "create table " + TasksContract.TABLE_NAME + " ("
                + TasksContract.Columns._ID + " integer primary key not null, "
                + TasksContract.Columns.TASKS_NAME + " text not null, "
                + TasksContract.Columns.TASKS_DESCRIPTION + " text, "
                + TasksContract.Columns.TASKS_SORTORDER + " integer);";
        Log.d(TAG, "onCreate: " + sSQL);
        db.execSQL(sSQL);

        addTimingsTable(db);
        addDurationsView(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: starts");
        switch (oldVersion) {
            case 1:
                addTimingsTable(db);
            case 2:
                addDurationsView(db);
                break;
            default:
                throw new   IllegalStateException("unknown version "+ newVersion);
        }
        Log.d(TAG, "onUpgrade: ends");
    }

    private void addTimingsTable(SQLiteDatabase db) {
        String sSQL = "create table " + TimingsContract.TABLE_NAME + " ("
                + TimingsContract.Columns._ID + " integer primary key not null, "
                + TimingsContract.Columns.TIMINGS_TASK_ID + " integer not null, "
                + TimingsContract.Columns.TIMINGS_START_TIME + " integer, "
                + TimingsContract.Columns.TIMINGS_DURATION + " integer);";
        Log.d(TAG, "onCreate: " + sSQL);
        db.execSQL(sSQL);

        sSQL = "create trigger Remove_Task " +
                "after delete on " + TasksContract.TABLE_NAME
                + " for each row"
                + " begin"
                + " delete from " + TimingsContract.TABLE_NAME
                + " where " + TimingsContract.Columns.TIMINGS_TASK_ID + " = old." + TasksContract.Columns._ID + ";"
                + " end;";
        Log.d(TAG, "onCreate: " + sSQL);
        db.execSQL(sSQL);
    }

    private void addDurationsView(SQLiteDatabase db) {
        String sSQL = "CREATE VIEW " + DurationsContract.TABLE_NAME
                + " AS SELECT " + TimingsContract.TABLE_NAME + "." + TimingsContract.Columns._ID + ", "
                + TasksContract.TABLE_NAME + "." + TasksContract.Columns.TASKS_NAME + ", "
                + TasksContract.TABLE_NAME + "." + TasksContract.Columns.TASKS_DESCRIPTION + ", "
                + TimingsContract.TABLE_NAME + "." + TimingsContract.Columns.TIMINGS_START_TIME + ","
                + " DATE(" + TimingsContract.TABLE_NAME + "." + TimingsContract.Columns.TIMINGS_START_TIME + ", 'unixepoch')"
                + " AS " + DurationsContract.Columns.DURATIONS_START_DATE + ","
                + " SUM(" + TimingsContract.TABLE_NAME + "." + TimingsContract.Columns.TIMINGS_DURATION + ")"
                + " AS " + DurationsContract.Columns.DURATIONS_DURATION
                + " FROM " + TasksContract.TABLE_NAME + " JOIN " + TimingsContract.TABLE_NAME
                + " ON " + TasksContract.TABLE_NAME + "." + TasksContract.Columns._ID + " = "
                + TimingsContract.TABLE_NAME + "." + TimingsContract.Columns.TIMINGS_TASK_ID
                + " GROUP BY " + DurationsContract.Columns.DURATIONS_START_DATE + ", " + DurationsContract.Columns.DURATIONS_NAME
                + ";";
        Log.d(TAG, sSQL);
        db.execSQL(sSQL);
    }
}
