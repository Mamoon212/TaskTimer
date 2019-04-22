package com.mo2a.example.tasktimerjava;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.BaseColumns;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DurationsReport extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        DatePickerDialog.OnDateSetListener,
        AppDialog.DialogEvents,
        View.OnClickListener {

    private static final String TAG = "DurationsReport";

    private static final int LOADER_ID = 1;

    public static final int DIALOG_FILTER = 1;
    public static final int DIALOG_DELETE = 2;

    private static final String SELECTION_PARAM = "SELECTION";
    private static final String SELECTION_ARGS_PARAM = "SELECTION_ARGS";
    private static final String SORT_ORDER_PARAM = "SORT_ORDER";

    public static final String DELETION_DATE = "DELETION";

    public static final String CURRENT_DATE = "CURRENT_DATE";
    public static final String DISPLAY_WEEK = "DISPLAY_WEEK";

    private Bundle args = new Bundle();
    private boolean displayWeek = true;
    private DurationsRVAdapter adapter;
    private final GregorianCalendar calendar = new GregorianCalendar();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_durations_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState != null) {
            long timeinmillis = savedInstanceState.getLong(CURRENT_DATE, 0);
            if (timeinmillis != 0) {
                calendar.setTimeInMillis(timeinmillis);
                calendar.clear(GregorianCalendar.HOUR_OF_DAY);
                calendar.clear(GregorianCalendar.MINUTE);
                calendar.clear(GregorianCalendar.SECOND);
            }
            displayWeek = savedInstanceState.getBoolean(DISPLAY_WEEK, true);
        }
        applyFilter();

        TextView name = findViewById(R.id.td_name_heading);
        name.setOnClickListener(this);
        TextView desc = findViewById(R.id.td_description_heading);
        if(desc != null){
            desc.setOnClickListener(this);
        }
        TextView start = findViewById(R.id.td_start_heading);
        start.setOnClickListener(this);
        TextView dur = findViewById(R.id.td_duration_heading);
        dur.setOnClickListener(this);

        RecyclerView recyclerView = findViewById(R.id.td_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (adapter == null) {
            adapter = new DurationsRVAdapter(this, null);
        }
        recyclerView.setAdapter(adapter);
        LoaderManager.getInstance(this).initLoader(LOADER_ID, args, this);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: starts");
        switch (v.getId()) {
            case R.id.td_name_heading:
                args.putString(SORT_ORDER_PARAM, DurationsContract.Columns.DURATIONS_NAME);
                break;
            case R.id.td_description_heading:
                args.putString(SORT_ORDER_PARAM, DurationsContract.Columns.DURATIONS_DESCRIPTION);
                break;
            case R.id.td_start_heading:
                args.putString(SORT_ORDER_PARAM, DurationsContract.Columns.DURATIONS_START_DATE);
                break;
            case R.id.td_duration_heading:
                args.putString(SORT_ORDER_PARAM, DurationsContract.Columns.DURATIONS_DURATION);
                break;
        }

        LoaderManager.getInstance(this).restartLoader(LOADER_ID, args, this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(CURRENT_DATE, calendar.getTimeInMillis());
        outState.putBoolean(DISPLAY_WEEK, displayWeek);
        super.onSaveInstanceState(outState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_report, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.rm_filter_period:
                displayWeek = !displayWeek;
                applyFilter();
                invalidateOptionsMenu();
                LoaderManager.getInstance(this).restartLoader(LOADER_ID, args, this);
                return true;

            case R.id.rm_filter_date:
                showDatePickerDialog(getString(R.string.filter_date_message), DIALOG_FILTER);
                return true;

            case R.id.rm_delete:
                showDatePickerDialog(getString(R.string.rm_delete_message), DIALOG_DELETE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.rm_filter_period);
        if (item != null) {
            if (displayWeek) {
                item.setIcon(R.drawable.ic_filter_1_black_24dp);
                item.setTitle(R.string.rm_title_filter_day);
            } else {
                item.setIcon(R.drawable.ic_filter_7_black_24dp);
                item.setTitle(R.string.rm_title_filter_week);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void showDatePickerDialog(String title, int dialogId) {
        Log.d(TAG, "showDatePickerDialog: starts");
        DialogFragment dialogFragment = new DatePickerFragment();

        Bundle args = new Bundle();
        args.putInt(DatePickerFragment.DATE_PICKER_ID, dialogId);
        args.putString(DatePickerFragment.DATE_PICKER_TITLE, title);
        args.putSerializable(DatePickerFragment.DATE_PICKER_DATE, calendar.getTime());

        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
        Log.d(TAG, "showDatePickerDialog: ends");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Log.d(TAG, "onDateSet: starts");
        int dialogId = (int) view.getTag();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        switch (dialogId) {
            case DIALOG_FILTER:
                applyFilter();
                LoaderManager.getInstance(this).restartLoader(LOADER_ID, args, this);
                break;

            case DIALOG_DELETE:
                String fromDate = DateFormat.getDateFormat(this)
                        .format(calendar.getTimeInMillis());
                AppDialog dialog = new AppDialog();
                Bundle args = new Bundle();
                args.putInt(AppDialog.DIALOG_ID, 1);
                args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.confirm_delete_message, fromDate) + "?");
                args.putLong(DELETION_DATE, calendar.getTimeInMillis());
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), null);
                break;

            default:
                throw new IllegalArgumentException("invalid");
        }
    }

    private void deleteRecords(Long timeInMillis) {
        Log.d(TAG, "deleteRecords: starts");
        long longDate = timeInMillis / 1000;
        String[] selectionArgs = new String[]{Long.toString(longDate)};
        String selection = TimingsContract.Columns.TIMINGS_START_TIME + " < ?";
        Log.d(TAG, "deleteRecords: prior to " + longDate);
        ContentResolver contentResolver = getContentResolver();
        contentResolver.delete(TimingsContract.CONTENT_URI, selection, selectionArgs);
        applyFilter();
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, args, this);

        Log.d(TAG, "deleteRecords: ends");
    }

    @Override
    public void onPositiveResult(int dialogId, Bundle args) {
        Log.d(TAG, "onPositiveResult: starts");
        long deleteDate = args.getLong(DELETION_DATE);
        deleteRecords(deleteDate);
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, args, this);
    }

    @Override
    public void onNegativeResult(int dialogId, Bundle args) {

    }

    @Override
    public void onCancel(int dialogId) {

    }

    private void applyFilter() {
        Log.d(TAG, "applyFilter: starts");
        if (displayWeek) {
            Date currentCalendarDate = calendar.getTime();
            int dayOfWeek = calendar.get(GregorianCalendar.DAY_OF_WEEK);
            int weekStart = calendar.getFirstDayOfWeek();
            Log.d(TAG, "applyFilter: first day is " + weekStart);
            Log.d(TAG, "applyFilter: day is" + dayOfWeek);
            Log.d(TAG, "applyFilter: date is " + calendar.getTime());
            calendar.set(GregorianCalendar.DAY_OF_WEEK, weekStart);
            String startDate = String.format(Locale.US, "%04d-%02d-%02d",
                    calendar.get(GregorianCalendar.YEAR),
                    calendar.get(GregorianCalendar.MONTH) + 1,
                    calendar.get(GregorianCalendar.DAY_OF_MONTH));

            calendar.add(GregorianCalendar.DATE, 6);
            String endDate = String.format(Locale.US, "%04d-%02d-%02d",
                    calendar.get(GregorianCalendar.YEAR),
                    calendar.get(GregorianCalendar.MONTH) + 1,
                    calendar.get(GregorianCalendar.DAY_OF_MONTH));

            String[] selectionArgs = new String[]{startDate, endDate};

            calendar.setTime(currentCalendarDate);
            Log.d(TAG, "applyFilter: 7 start and end are: " + startDate + endDate);
            args.putString(SELECTION_PARAM, "StartDate Between ? AND ?");
            args.putStringArray(SELECTION_ARGS_PARAM, selectionArgs);
        } else {
            String startDate = String.format(Locale.US, "%04d-%02d-%02d",
                    calendar.get(GregorianCalendar.YEAR),
                    calendar.get(GregorianCalendar.MONTH) + 1,
                    calendar.get(GregorianCalendar.DAY_OF_MONTH));
            String[] selectionArgs = new String[]{startDate};
            Log.d(TAG, "applyFilter: 1 " + startDate);
            args.putString(SELECTION_PARAM, "StartDate = ?");
            args.putStringArray(SELECTION_ARGS_PARAM, selectionArgs);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == LOADER_ID) {
            String[] projection = {BaseColumns._ID,
                    DurationsContract.Columns.DURATIONS_NAME,
                    DurationsContract.Columns.DURATIONS_DESCRIPTION,
                    DurationsContract.Columns.DURATIONS_START_TIME,
                    DurationsContract.Columns.DURATIONS_START_DATE,
                    DurationsContract.Columns.DURATIONS_DURATION};
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;

            if (args != null) {
                selection = args.getString(SELECTION_PARAM);
                selectionArgs = args.getStringArray(SELECTION_ARGS_PARAM);
                sortOrder = args.getString(SORT_ORDER_PARAM);
            }

            return new CursorLoader(this,
                    DurationsContract.CONTENT_URI,
                    projection, selection, selectionArgs, sortOrder);
        }
        throw new InvalidParameterException("called with invalid id " + id);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: starts");
        adapter.swapCursor(data);
        int count = adapter.getItemCount();
        Log.d(TAG, "onLoadFinished: count is " + count);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: starts");
        adapter.swapCursor(null);
    }

}
