package org.stepic.droid.ui.dialogs;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.FileUtil;
import org.stepic.droid.ui.util.LogoutSuccess;

import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

public class LogoutAreYouSureDialog extends DialogFragment {

    public static LogoutAreYouSureDialog newInstance() {
        return new LogoutAreYouSureDialog();
    }

    @Inject
    IShell shell;
    @Inject
    DatabaseFacade databaseFacade;
    @Inject
    DownloadManager systemDownloadManager;
    @Inject
    UserPreferences userPreferences;
    @Inject
    ThreadPoolExecutor threadPoolExecutor;
    @Inject
    SharedPreferenceHelper sharedPreferenceHelper;
    @Inject
    Analytic analytic;

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
                        analytic.reportEvent(Analytic.Interaction.CLICK_YES_LOGOUT);

                        final File directoryForClean = userPreferences.getUserDownloadFolder();
                        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                List<DownloadEntity> downloadEntities = databaseFacade.getAllDownloadEntities();
                                for (DownloadEntity de : downloadEntities) {
                                    systemDownloadManager.remove(de.getDownloadId());
                                }

                                FileUtil.cleanDirectory(directoryForClean);

                                databaseFacade.dropDatabase();
                                return null;
                            }
                        };
                        task.executeOnExecutor(threadPoolExecutor);

                        ((LogoutSuccess) getActivity()).onLogout(); //it is not after asynctask.
                    }
                })
                .setNegativeButton(R.string.no, null);

        return builder.create();
    }
}
