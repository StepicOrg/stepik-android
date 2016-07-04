package org.stepic.droid.view.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IScreenManager;
import org.stepic.droid.util.AppConstants;

import javax.inject.Inject;

public class UnauthorizedDialogFragment extends DialogFragment {
    public static DialogFragment newInstance() {
        return new UnauthorizedDialogFragment();
    }

    @Inject
    IScreenManager screenManager;

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
                        YandexMetrica.reportEvent(AppConstants.AUTH_FROM_DIALOG);
                        screenManager.showLaunchScreen(getContext(), false);
                    }
                })
                .setNegativeButton(R.string.no, null);

        return builder.create();
    }
}
