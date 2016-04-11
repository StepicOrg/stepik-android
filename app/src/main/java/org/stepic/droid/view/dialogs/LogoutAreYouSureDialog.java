package org.stepic.droid.view.dialogs;

import android.app.Dialog;
import android.app.DownloadManager;
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
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.FileUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

public class LogoutAreYouSureDialog extends DialogFragment {

    public static LogoutAreYouSureDialog newInstance() {
        return new LogoutAreYouSureDialog();
    }

    @Inject
    IShell mShell;
    @Inject
    DatabaseFacade mDatabaseFacade;
    @Inject
    DownloadManager mSystemDownloadManager;
    @Inject
    UserPreferences mUserPreferences;
    @Inject
    ThreadPoolExecutor mThreadPoolExecutor;
    @Inject
    SharedPreferenceHelper mSharedPreferenceHelper;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainApplication.component().inject(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        builder.setTitle(R.string.title_clear_cache_dialog)
                .setMessage(R.string.are_you_sure_logout)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_YES_LOGOUT);

                        final File directoryForClean = mUserPreferences.getUserDownloadFolder();
                        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                List<DownloadEntity> downloadEntities = mDatabaseFacade.getAllDownloadEntities();
                                for (DownloadEntity de : downloadEntities) {
                                    mSystemDownloadManager.remove(de.getDownloadId());
                                }

                                FileUtil.cleanDirectory(directoryForClean);

                                mDatabaseFacade.dropDatabase();
                                return null;
                            }
                        };
                        task.executeOnExecutor(mThreadPoolExecutor);

                        mSharedPreferenceHelper.deleteAuthInfo();
                        mShell.getScreenProvider().showLaunchScreen(MainApplication.getAppContext(), false);
                    }
                })
                .setNegativeButton(R.string.no, null);

        return builder.create();
    }
}
