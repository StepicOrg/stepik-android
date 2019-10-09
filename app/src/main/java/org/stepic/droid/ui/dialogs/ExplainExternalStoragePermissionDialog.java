package org.stepic.droid.ui.dialogs;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.app.DialogFragment;
import androidx.core.app.Fragment;
import android.support.v7.app.AlertDialog;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.util.AppConstants;

public class ExplainExternalStoragePermissionDialog extends DialogFragment {

    public static DialogFragment newInstance() {
        return new ExplainExternalStoragePermissionDialog();
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.allow_question)
                .setMessage(R.string.explain_permission)
                .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Fragment targetFragment = getTargetFragment();
                        if (targetFragment != null) {
                            targetFragment.requestPermissions(
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    AppConstants.REQUEST_EXTERNAL_STORAGE);
                        } else {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    AppConstants.REQUEST_EXTERNAL_STORAGE);
                        }
                    }
                })
                .setNegativeButton(R.string.deny, null);
        return builder.create();
    }
}