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
import org.stepic.droid.model.StorageOption;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.util.AppConstants;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

public class ChooseStorageDialog extends DialogFragment {

    @Inject
    ThreadPoolExecutor threadPoolExecutor;

    @Inject
    UserPreferences mUserPreferences;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainApplication.component().inject(this);

        //fixme get From User Prefs
        final List<StorageOption> storageOptions = mUserPreferences.getStorageOptionList();
        String[] headers = new String[storageOptions.size()];
        int indexChosen = -1; //// FIXME: 08.06.16 change to 0
        for (int i = 0; i < headers.length; i++) {
            headers[i] = storageOptions.get(i).getPresentableInfo();
            if (storageOptions.get(i).isChosen()) {
                indexChosen = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        builder.setTitle(R.string.choose_storage_title)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        YandexMetrica.reportEvent(AppConstants.METRICA_CANCEL_CHOOSE_STORE_CLICK);
                    }
                })
                .setSingleChoiceItems(headers,
                        indexChosen,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
//                                        mUserPreferences.storeQualityVideo(mPositionToQualityMap.get(which));
                                        //fixme store to userPrefs PLS and show progress if need move. AND MOVE
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        super.onPostExecute(aVoid);
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                    }
                                };
                                task.executeOnExecutor(threadPoolExecutor);
                            }
                        });

        return builder.create();
    }

}
