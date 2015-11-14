package org.stepic.droid.view.dialogs;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.CleanerUtil;

import java.util.List;

import javax.inject.Inject;

public class ClearCacheDialogFragment extends DialogFragment {

    @Inject
    DatabaseManager mDatabaseManager;
    @Inject
    UserPreferences userPreferences;
    @Inject
    DownloadManager mSystemDownloadManager;

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
                        YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_YES_CLEAR_CACHE);
                        //// FIXME: 22.10.15 do it in background
                        List<DownloadEntity> downloadEntities = mDatabaseManager.getAllDownloadEntities();
                        for (DownloadEntity de : downloadEntities) {
                            mSystemDownloadManager.remove(de.getDownloadId());
                        }

                        CleanerUtil.CleanDirectory(userPreferences.getDownloadFolder());

                        mDatabaseManager.dropDatabase();
                    }
                })
                .setNegativeButton(R.string.no, null);
        setCancelable(false);

        return builder.create();
    }
}
