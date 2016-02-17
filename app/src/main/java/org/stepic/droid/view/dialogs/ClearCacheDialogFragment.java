package org.stepic.droid.view.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.FileUtil;

import java.util.List;

import javax.inject.Inject;

public class ClearCacheDialogFragment extends DialogFragment {

    @Inject
    DatabaseFacade mDatabaseFacade;
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
                        List<DownloadEntity> downloadEntities = mDatabaseFacade.getAllDownloadEntities();
                        for (DownloadEntity de : downloadEntities) {
                            mSystemDownloadManager.remove(de.getDownloadId());
                        }

                        FileUtil.cleanDirectory(userPreferences.getUserDownloadFolder());

                        mDatabaseFacade.dropDatabase();
                        sendResult(Activity.RESULT_OK);
                        Toast.makeText(getContext(), R.string.cache_cleared, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.no, null);

        return builder.create();
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
