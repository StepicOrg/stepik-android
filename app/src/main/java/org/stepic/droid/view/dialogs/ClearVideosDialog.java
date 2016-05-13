package org.stepic.droid.view.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.squareup.otto.Bus;
import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.loading.FinishDeletingLoadEvent;
import org.stepic.droid.events.loading.StartDeletingLoadEvent;
import org.stepic.droid.events.steps.ClearAllDownloadWithoutAnimationEvent;
import org.stepic.droid.model.Step;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DbParseHelper;

import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

public  class ClearVideosDialog extends DialogFragment {

    public static final String KEY_STRING_IDS = "step_ids";;
    @Inject
    DatabaseFacade mDatabaseFacade;
    @Inject
    CleanManager mCleanManager;
    @Inject
    Bus mBus;

    @Inject
    ThreadPoolExecutor threadPoolExecutor;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainApplication.component().inject(this);
        Bundle bundle = getArguments();
        String stringIds = bundle.getString(KEY_STRING_IDS);
        final long[] stepIds = DbParseHelper.parseStringToLongArray(stringIds);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        builder.setTitle(R.string.title_clear_cache_dialog)
                .setMessage(R.string.clear_videos)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        YandexMetrica.reportEvent(AppConstants.METRICA_YES_CLEAR_VIDEOS);
                        mBus.post(new ClearAllDownloadWithoutAnimationEvent(stepIds));
                        if (stepIds == null) return;


                        AsyncTask task = new AsyncTask() {

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                mBus.post(new StartDeletingLoadEvent());

                            }

                            @Override
                            protected Object doInBackground(Object[] params) {
                                for (long stepId : stepIds) {
                                    Step step = mDatabaseFacade.getStepById(stepId);
                                    mCleanManager.removeStep(step);
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Object o) {
                                mBus.post(new FinishDeletingLoadEvent());
                            }
                        };
                        task.executeOnExecutor(threadPoolExecutor);
                    }
                })
                .setNegativeButton(R.string.no, null);

        return builder.create();
    }
}
