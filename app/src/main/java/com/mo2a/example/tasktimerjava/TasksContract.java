package com.mo2a.example.tasktimerjava;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.mo2a.example.tasktimerjava.AppProvider.CONTENT_AUTHORITY;
import static com.mo2a.example.tasktimerjava.AppProvider.CONTENT_AUTHORITY_URI;

public class TasksContract {
    static final String TABLE_NAME="Tasks";
    public static class Columns{
        public static final String _ID= BaseColumns._ID;
        public static final String TASKS_NAME= "Name";
        public static final String TASKS_DESCRIPTION= "Description";
        public static final String TASKS_SORTORDER= "SortOrder";

        private Columns(){

        }
    }

    public static final Uri CONTENT_URI = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME);
    static final String CONTENT_TYPE= "vnd.android.cursor.dir/vnd."+ CONTENT_AUTHORITY+ "."+ TABLE_NAME;
    static final String CONTENT_ITEM_TYPE= "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY+ "."+ TABLE_NAME;

    public static Uri buildTaskUri(long taskId){
        return ContentUris.withAppendedId(CONTENT_URI, taskId);
    }

    public static long getTaskId(Uri uri){
        return ContentUris.parseId(uri);
    }
}
