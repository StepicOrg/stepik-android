package org.stepic.droid.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.Step;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.IStoreStateManager;
import org.stepic.droid.store.operations.DatabaseFacade;

import java.io.File;

import javax.inject.Inject;

public class DownloadCompleteReceiver extends BroadcastReceiver {

    @Inject
    UserPreferences mUserPrefs;
    @Inject
    DatabaseFacade mDatabaseFacade;
    @Inject
    IStoreStateManager mStoreStateManager;

    public DownloadCompleteReceiver() {
        MainApplication.component().inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                //critical section:

                DownloadEntity downloadEntity = mDatabaseFacade.getDownloadEntityIfExist(referenceId);
                if (downloadEntity != null) {
                    long video_id = downloadEntity.getVideoId();
                    long step_id = downloadEntity.getStepId();
                    mDatabaseFacade.deleteDownloadEntityByDownloadId(referenceId);

                    File downloadFolderAndFile = new File(mUserPrefs.getUserDownloadFolder(), video_id + "");
                    String path = Uri.fromFile(downloadFolderAndFile).getPath();
                    CachedVideo cachedVideo = new CachedVideo(step_id, video_id, path, downloadEntity.getThumbnail());
                    cachedVideo.setQuality(downloadEntity.getQuality());
                    mDatabaseFacade.addVideo(cachedVideo);

                    Step step = mDatabaseFacade.getStepById(step_id);
                    step.setIs_cached(true);
                    step.setIs_loading(false);
                    mDatabaseFacade.updateOnlyCachedLoadingStep(step);
                    mStoreStateManager.updateUnitLessonState(step.getLesson());
                }
                return null;
                //end critical section
            }
        };
        task.execute();
    }

}