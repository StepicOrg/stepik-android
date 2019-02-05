package org.stepik.android.view.course_calendar.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.util.AppConstants;

public class ExplainCalendarPermissionDialog extends DialogFragment {

    public static DialogFragment newInstance() {
        return new ExplainCalendarPermissionDialog();
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.allow_question)
                .setMessage(R.string.explain_calendar_permission)
                .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.WRITE_CALENDAR},
                                AppConstants.REQUEST_CALENDAR_PERMISSION);
                    }
                })
                .setNegativeButton(R.string.deny, null);
        return builder.create();
    }
}