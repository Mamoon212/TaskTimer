package com.mo2a.example.tasktimerjava;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.mo2a.example.tasktimerjava.AppProvider.CONTENT_AUTHORITY;
import static com.mo2a.example.tasktimerjava.AppProvider.CONTENT_AUTHORITY_URI;

public class TimingsContract {
    static final String TABLE_NAME="Timings";
    public static class Columns{
        public static final String _ID= BaseColumns._ID;
        public static final String TIMINGS_TASK_ID= "taskId";
        public static final String TIMINGS_START_TIME= "startTime";
        public static final String TIMINGS_DURATION= "duration";

        private Columns(){

        }
    }

    public static final Uri CONTENT_URI = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME);
    static final String CONTENT_TYPE= "vnd.android.cursor.dir/vnd."+ CONTENT_AUTHORITY+ "."+ TABLE_NAME;
    static final String CONTENT_ITEM_TYPE= "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY+ "."+ TABLE_NAME;

    public static Uri buildTimingUri(long taskId){
        return ContentUris.withAppendedId(CONTENT_URI, taskId);
    }

    public static long getTimingId(Uri uri){
        return ContentUris.parseId(uri);
    }
}
