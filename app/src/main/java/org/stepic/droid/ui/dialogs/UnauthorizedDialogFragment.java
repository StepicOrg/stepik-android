package org.stepic.droid.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.ScreenManager;

import javax.inject.Inject;

public class UnauthorizedDialogFragment extends DialogFragment {
    public static DialogFragment newInstance() {
        return new UnauthorizedDialogFragment();
    }

    @Inject
    ScreenManager screenManager;

    @Inject
    Analytic analytic;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainApplication.component().inject(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.authorization)
                .setMessage(R.string.unauthorization_detail)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        analytic.reportEvent(Analytic.Interaction.AUTH_FROM_DIALOG_FOR_UNAUTHORIZED_USER);
                        screenManager.showLaunchScreen(getActivity());
                    }
                })
                .setNegativeButton(R.string.no, null);

        return builder.create();
    }
}
