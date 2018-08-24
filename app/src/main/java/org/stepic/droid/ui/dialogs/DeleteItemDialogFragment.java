package org.stepic.droid.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.stepic.droid.R;

public class DeleteItemDialogFragment extends DialogFragment {
    public final static String TAG = "DeleteItemDialogFragment";
    public final static String deletePositionKey = "deletePositionKey";

    public static DeleteItemDialogFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(deletePositionKey, position);
        DeleteItemDialogFragment fragment = new DeleteItemDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int position = getArguments().getInt(deletePositionKey);

        final Intent data = new Intent();
        data.putExtra(deletePositionKey, position);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_confirmation)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
                    }
                })
                .setNegativeButton(R.string.no, null);
        return builder.create();
    }
}
