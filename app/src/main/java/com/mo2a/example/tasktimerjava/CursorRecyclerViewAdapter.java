package com.mo2a.example.tasktimerjava;

import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.TaskViewHolder> {
    private static final String TAG = "CursorRecyclerViewAdapt";
    private Cursor cursor;
    private OnTaskClickListener listener;

    interface OnTaskClickListener {
        void onEditClick(@NonNull Task task);
        void onDeleteClick(@NonNull Task task);
        void onTaskLongClick(@NonNull Task task);
    }

    public CursorRecyclerViewAdapter(Cursor cursor, OnTaskClickListener listener) {
        Log.d(TAG, "CursorRecyclerViewAdapter: called");
        this.cursor = cursor;
        this.listener = listener;
    }

    public void setListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list_items, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        if (cursor == null || cursor.getCount() == 0) {
            Log.d(TAG, "onBindViewHolder: providing instructions");
            holder.name.setText(R.string.instructions_name);
            holder.description.setText(R.string.instructions_descriptions);
            holder.edit.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);
        } else {
            if (!cursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            final Task task = new Task(cursor.getLong(cursor.getColumnIndex(TasksContract.Columns._ID)),
                    cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASKS_NAME)),
                    cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASKS_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndex(TasksContract.Columns.TASKS_SORTORDER)));

            holder.name.setText(task.getName());
            holder.description.setText(task.getDescription());
            holder.edit.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
            View.OnClickListener buttonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.tli_edit:
                            if (listener != null) {
                                listener.onEditClick(task);
                            }
                            break;
                        case R.id.tli_delete:
                            if (listener != null) {
                                listener.onDeleteClick(task);
                            }
                            break;
                    }
                }
            };

            View.OnLongClickListener buttLongListener= new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d(TAG, "onLongClick: starts");
                    if(listener != null){
                        listener.onTaskLongClick(task);
                        return true;
                    }
                    return false;
                }
            };

            holder.edit.setOnClickListener(buttonListener);
            holder.delete.setOnClickListener(buttonListener);
            holder.itemView.setOnLongClickListener(buttLongListener);
        }
    }

    @Override
    public int getItemCount() {
        if (cursor == null || cursor.getCount() == 0) {
            return 1;
        } else {
            return cursor.getCount();
        }
    }

    Cursor swapCursor(Cursor nCursor) {
        if (nCursor == cursor) {
            return null;
        }
        int num= getItemCount();
        final Cursor oldCursor = cursor;
        cursor = nCursor;
        if (nCursor != null) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, num);
        }
        return oldCursor;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "TaskViewHolder";
        TextView name;
        TextView description;
        ImageButton edit;
        ImageButton delete;
        View itemView;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.tli_name);
            this.description = itemView.findViewById(R.id.tli_desciption);
            this.edit = itemView.findViewById(R.id.tli_edit);
            this.delete = itemView.findViewById(R.id.tli_delete);
            this.itemView= itemView;
        }
    }
}
