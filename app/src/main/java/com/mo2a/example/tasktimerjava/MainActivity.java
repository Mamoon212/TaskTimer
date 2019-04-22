package com.mo2a.example.tasktimerjava;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements CursorRecyclerViewAdapter.OnTaskClickListener,
        AddEditActivityFragment.OnSaveClicked, AppDialog.DialogEvents {
    private static final String TAG = "MainActivity";
    private boolean twoPane = false;
    public static final int DIALOG_ID_DELETE = 1;
    public static final int DIALOG_ID_CANCEL_EDIT = 2;
    public static final int DIALOG_ID_CANCEL_EDIT_UP = 3;
    private AlertDialog dialog = null;
    private Timing currentTiming = null;
    public static final String TIMING_KEY= "KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        if (findViewById(R.id.task_details_container) != null) {
//            twoPane = true;
//        }
        twoPane = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean editing = fragmentManager.findFragmentById(R.id.task_details_container) != null;
        View addEditLayout = findViewById(R.id.task_details_container);
        View mainFrag = findViewById(R.id.fragment);

        if (twoPane) {
            mainFrag.setVisibility(View.VISIBLE);
            addEditLayout.setVisibility(View.VISIBLE);
        } else if (editing) {
            mainFrag.setVisibility(View.GONE);
        } else {
            mainFrag.setVisibility(View.VISIBLE);
            addEditLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(TIMING_KEY,currentTiming);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentTiming =(Timing) savedInstanceState.getSerializable(TIMING_KEY);
        setTimingText(currentTiming);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(BuildConfig.DEBUG){
            MenuItem generate= menu.findItem(R.id.menumain_generate);
            generate.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.menumain_addTask:
                taskEditRequest(null);
                break;
            case R.id.menumain_showDurations:
                startActivity(new Intent(this, DurationsReport.class));
                break;
            case R.id.menumain_settings:
                break;
            case R.id.menumain_showAbout:
                showAboutDialog();
                break;
            case android.R.id.home:
                Log.d(TAG, "onOptionsItemSelected: hi");
                AddEditActivityFragment fragment = (AddEditActivityFragment) getSupportFragmentManager().findFragmentById(R.id.task_details_container);
                if (fragment.canClose()) {
                    return super.onOptionsItemSelected(item);
                } else {
                    showConfirmationDialog(DIALOG_ID_CANCEL_EDIT_UP);
                    return true;
                }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @SuppressLint("SetTextI18n")
    public void showAboutDialog() {
        @SuppressLint("InflateParams") View messageView = getLayoutInflater().inflate(R.layout.about, null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setView(messageView);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);


        TextView tv = messageView.findViewById(R.id.about_version);
        tv.setText("v" + BuildConfig.VERSION_NAME);
        dialog.show();
    }

    @Override
    public void onEditClick(@NonNull Task task) {
        taskEditRequest(task);
    }

    @Override
    public void onDeleteClick(@NonNull Task task) {
        Log.d(TAG, "onDeleteClick: starts");
        AppDialog appDialog = new AppDialog();
        Bundle args = new Bundle();
        args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_DELETE);
        args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.deldiag_message, task.getId(), task.getName()));
        args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.deldiag_positive_caption);
        args.putLong("TaskId", task.getId());
        appDialog.setArguments(args);
        appDialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onTaskLongClick(@NonNull Task task) {
        Log.d(TAG, "onTaskLongClick: starts");
        if (currentTiming != null) {
            if (task.getId() == currentTiming.getTask().getId()) {
                saveTiming(currentTiming);
                currentTiming = null;
                setTimingText(null);
            } else {
                saveTiming(currentTiming);
                currentTiming = new Timing(task);
                setTimingText(currentTiming);
            }
        } else {
            currentTiming = new Timing(task);
            setTimingText(currentTiming);
        }
    }

    private void saveTiming(@NonNull Timing currentTiming) {
        Log.d(TAG, "saveTiming: starts");
        currentTiming.setDuration();
        ContentResolver contentResolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(TimingsContract.Columns.TIMINGS_TASK_ID, currentTiming.getTask().getId());
        values.put(TimingsContract.Columns.TIMINGS_START_TIME, currentTiming.getStartTime());
        values.put(TimingsContract.Columns.TIMINGS_DURATION, currentTiming.getDuration());
        contentResolver.insert(TimingsContract.CONTENT_URI, values);
        Log.d(TAG, "saveTiming: ending");
    }

    private void setTimingText(Timing timing) {
        TextView taskname = findViewById(R.id.current_task);
        if (timing != null) {
            taskname.setText("Timing " + currentTiming.getTask().getName());
        } else {
            taskname.setText(R.string.no_task_message);
        }
    }

    @Override
    public void onSaveClicked() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.task_details_container);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }

        View addEditLayout = findViewById(R.id.task_details_container);
        View mainFrag = findViewById(R.id.fragment);

        if (!twoPane) {
            addEditLayout.setVisibility(View.GONE);
            mainFrag.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPositiveResult(int dialogId, Bundle args) {
        Log.d(TAG, "onPositiveResult: called");
        switch (dialogId) {
            case DIALOG_ID_DELETE:
                long taskId = args.getLong("TaskId");
                getContentResolver().delete(TasksContract.buildTaskUri(taskId), null, null);
                break;

            case DIALOG_ID_CANCEL_EDIT:
            case DIALOG_ID_CANCEL_EDIT_UP:
                break;
        }

    }

    @Override
    public void onNegativeResult(int dialogId, Bundle args) {
        Log.d(TAG, "onNegativeResult: called");
        switch (dialogId) {
            case DIALOG_ID_DELETE:
                break;
            case DIALOG_ID_CANCEL_EDIT:
            case DIALOG_ID_CANCEL_EDIT_UP:
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.task_details_container);
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(fragment)
                            .commit();
                    if (twoPane) {
                        if (dialogId == DIALOG_ID_CANCEL_EDIT) {
                            finish();
                        }
                    } else {
                        View addEditLayout = findViewById(R.id.task_details_container);
                        View mainFrag = findViewById(R.id.fragment);
                        addEditLayout.setVisibility(View.GONE);
                        mainFrag.setVisibility(View.VISIBLE);
                    }
                } else {
                    finish();
                }
                break;
        }

    }

    @Override
    public void onCancel(int dialogId) {
        Log.d(TAG, "onCancel: called");
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddEditActivityFragment fragment = (AddEditActivityFragment) fragmentManager.findFragmentById(R.id.task_details_container);
        if ((fragment == null) || (fragment.canClose())) {
            super.onBackPressed();
        } else {
            showConfirmationDialog(DIALOG_ID_CANCEL_EDIT);
        }
    }

    private void taskEditRequest(Task task) {
        Log.d(TAG, "taskEditRequest: starts");
        Log.d(TAG, "taskEditRequest: in two pane mode");
        AddEditActivityFragment fragment = new AddEditActivityFragment();
        Bundle args = new Bundle();
        args.putSerializable(Task.class.getSimpleName(), task);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.task_details_container, fragment).commit();
        if (!twoPane) {
            View addEditLayout = findViewById(R.id.task_details_container);
            View mainFrag = findViewById(R.id.fragment);
            mainFrag.setVisibility(View.GONE);
            addEditLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showConfirmationDialog(int dialogId) {
        AppDialog dialog = new AppDialog();
        Bundle args = new Bundle();
        args.putInt(AppDialog.DIALOG_ID, dialogId);
        args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.cancelEditDial_message));
        args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.cancelEditDial_pos_cap);
        args.putInt(AppDialog.DIALOG_NEGATIVE_RID, R.string.cancelEditDial_neg_cap);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), null);
    }
}
