package org.stepic.droid.view.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.AppConstants;

import javax.inject.Inject;

public class LogoutAreYouSureDialog extends DialogFragment {

    @Inject
    IShell mShell;

    @Inject
    DatabaseManager mDbManager;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainApplication.component().inject(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        builder.setTitle(R.string.title_clear_cache_dialog)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_YES_LOGOUT);


                        SharedPreferenceHelper helper = mShell.getSharedPreferenceHelper();
                        helper.deleteAuthInfo();
                        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                mDbManager.clearCacheCourses(DatabaseManager.Table.enrolled);
                                return null;
                            }
                        };
                        task.execute();
                        mShell.getScreenProvider().showLaunchScreen(MainApplication.getAppContext(), false);
                    }
                })
                .setNegativeButton(R.string.no, null);

        return builder.create();
    }
}
