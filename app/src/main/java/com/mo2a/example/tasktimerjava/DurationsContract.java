package com.mo2a.example.tasktimerjava;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.mo2a.example.tasktimerjava.AppProvider.CONTENT_AUTHORITY;
import static com.mo2a.example.tasktimerjava.AppProvider.CONTENT_AUTHORITY_URI;

public class DurationsContract {

    static final String TABLE_NAME = "vwTaskDurations";

    // Durations fields
    public static class Columns {
        public static final String _ID = BaseColumns._ID;
        public static final String DURATIONS_NAME = TasksContract.Columns.TASKS_NAME;
        public static final String DURATIONS_DESCRIPTION = TasksContract.Columns.TASKS_DESCRIPTION;
        public static final String DURATIONS_START_TIME = TimingsContract.Columns.TIMINGS_START_TIME;
        public static final String DURATIONS_START_DATE = "StartDate";
        public static final String DURATIONS_DURATION = TimingsContract.Columns.TIMINGS_DURATION;


        private Columns() {
            // private constructor to prevent instantiation
        }
    }

    /**
     * The URI to access the Durations view
     */
    public static final Uri CONTENT_URI = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME);

    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + "." + TABLE_NAME;
    static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + "." + TABLE_NAME;

    public static long getDurationId(Uri uri) {
        return ContentUris.parseId(uri);
    }

}












