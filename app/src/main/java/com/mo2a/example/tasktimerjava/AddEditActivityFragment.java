package com.mo2a.example.tasktimerjava;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddEditActivityFragment extends Fragment {
    private static final String TAG = "AddEditActivityFragment";

    public enum FragmentEditMode {EDIT, ADD}

    private FragmentEditMode mode;

    private EditText nameText;
    private EditText descriptionText;
    private EditText sortOrderText;
    private OnSaveClicked saveListener;

    interface OnSaveClicked {
        void onSaveClicked();
    }

    public AddEditActivityFragment() {
    }

    boolean canClose() {
        return false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: starts");
        super.onAttach(context);
        Activity activity = getActivity();
        if (!(activity instanceof OnSaveClicked)&&activity!=null) {
            throw new ClassCastException(activity.getClass().getSimpleName() + " must implement OnSaveClicked");
        } else {
            saveListener = (OnSaveClicked) activity;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar;
        if (getActivity() != null) {
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        saveListener = null;
        ActionBar actionBar;
        if (getActivity() != null) {
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);
        nameText = view.findViewById(R.id.addedit_name);
        descriptionText = view.findViewById(R.id.addedit_description);
        sortOrderText = view.findViewById(R.id.addedit_sortOrder);
        Button saveButton = view.findViewById(R.id.addedit_save);

        Bundle arguments = getArguments();
        final Task task;
        if (arguments != null) {
            Log.d(TAG, "onCreateView: getting task details");
            task = (Task) arguments.getSerializable(Task.class.getSimpleName());
            if (task != null) {
                Log.d(TAG, "onCreateView: editing");
                nameText.setText(task.getName());
                descriptionText.setText(task.getDescription());
                sortOrderText.setText(Integer.toString(task.getSortOrder()));
                mode = FragmentEditMode.EDIT;
            } else {
                Log.d(TAG, "onCreateView: adding new task");
                mode = FragmentEditMode.ADD;
            }
        } else {
            task = null;
            Log.d(TAG, "onCreateView: no argument, new record");
            mode = FragmentEditMode.ADD;
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int so;
                if (sortOrderText.length() > 0) {
                    so = Integer.parseInt(sortOrderText.getText().toString());
                } else {
                    so = 0;
                }
                ContentResolver contentResolver;
                if (getActivity() != null) {
                    contentResolver = getActivity().getContentResolver();
                    ContentValues contentValues = new ContentValues();

                    switch (mode) {
                        case EDIT:
                            if (task == null) {
                                break;
                            }
                            if (!nameText.getText().toString().equals(task.getName())) {
                                contentValues.put(TasksContract.Columns.TASKS_NAME, nameText.getText().toString());
                            }
                            if (!descriptionText.getText().toString().equals(task.getDescription())) {
                                contentValues.put(TasksContract.Columns.TASKS_DESCRIPTION, descriptionText.getText().toString());
                            }
                            if (so != task.getSortOrder()) {
                                contentValues.put(TasksContract.Columns.TASKS_SORTORDER, so);
                            }
                            if (contentValues.size() != 0) {
                                Log.d(TAG, "onClick: updating task");
                                contentResolver.update(TasksContract.buildTaskUri(task.getId()), contentValues, null, null);
                            }
                            break;

                        case ADD:
                            if (nameText.length() > 0) {
                                Log.d(TAG, "onClick: adding new task");
                                contentValues.put(TasksContract.Columns.TASKS_NAME, nameText.getText().toString());
                                contentValues.put(TasksContract.Columns.TASKS_DESCRIPTION, descriptionText.getText().toString());
                                contentValues.put(TasksContract.Columns.TASKS_SORTORDER, so);
                                contentResolver.insert(TasksContract.CONTENT_URI, contentValues);
                            }
                            break;

                    }
                    Log.d(TAG, "onClick: done editing");
                    if (saveListener != null) {
                        saveListener.onSaveClicked();
                    }
                }
            }
        });
        Log.d(TAG, "onCreateView: exiting");
        return view;
    }
}
