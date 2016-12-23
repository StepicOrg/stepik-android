package org.stepic.droid.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.presenters.ProfileMainFeedPresenter;

import javax.inject.Inject;

public class LogoutAreYouSureDialog extends DialogFragment {

    public static LogoutAreYouSureDialog newInstance() {
        return new LogoutAreYouSureDialog();
    }

    @Inject
    ProfileMainFeedPresenter profileMainFeedPresenter;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainApplication.component().inject(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_confirmation)
                .setMessage(R.string.are_you_sure_logout)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        profileMainFeedPresenter.logout();
                    }
                })
                .setNegativeButton(R.string.no, null);

        return builder.create();
    }
}
