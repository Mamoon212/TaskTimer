package com.mo2a.example.tasktimerjava;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AppDialog extends DialogFragment {
    private static final String TAG = "AppDialog";

    public static final String DIALOG_ID = "id";
    public static final String DIALOG_MESSAGE = "message";
    public static final String DIALOG_POSITIVE_RID = "positive_rid";
    public static final String DIALOG_NEGATIVE_RID = "negative_rid";

    interface DialogEvents {
        void onPositiveResult(int dialogId, Bundle args);

        void onNegativeResult(int dialogId, Bundle args);

        void onCancel(int dialogId);
    }

    private DialogEvents dialogEvents;

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: starts, activity is " + context.toString());
        super.onAttach(context);

        if (!(context instanceof DialogEvents)) {
            throw new ClassCastException(context.toString() + "must implement DialogEvents");
        }

        dialogEvents = (DialogEvents) context;
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: starts");
        super.onDetach();
        dialogEvents = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: starts");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Bundle arguments = getArguments();
        final int dialogId;
        String messageString;
        int positiveStringId;
        int negativeStringId;

        if (arguments != null) {
            dialogId = arguments.getInt(DIALOG_ID);
            messageString = arguments.getString(DIALOG_MESSAGE);
            if (dialogId == 0 || messageString == null) {
                throw new IllegalArgumentException("not present");
            }
            positiveStringId = arguments.getInt(DIALOG_POSITIVE_RID);
            negativeStringId = arguments.getInt(DIALOG_NEGATIVE_RID);
            if (positiveStringId == 0) {
                positiveStringId = R.string.ok;
            }
            if (negativeStringId == 0) {
                negativeStringId = R.string.cancel;
            }
        } else {
            throw new IllegalArgumentException("must pass arguments");
        }

        builder.setMessage(messageString)
                .setPositiveButton(positiveStringId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialogEvents != null) {
                            dialogEvents.onPositiveResult(dialogId, arguments);

                        }
                    }
                })
                .setNegativeButton(negativeStringId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialogEvents != null) {
                            dialogEvents.onNegativeResult(dialogId, arguments);
                        }
                    }
                });
        return builder.create();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        Log.d(TAG, "onCancel: starts");
        if (dialogEvents != null) {
            int dialogId = getArguments().getInt(DIALOG_ID);
            dialogEvents.onCancel(dialogId);

        }
    }
}
