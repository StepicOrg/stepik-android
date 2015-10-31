package org.stepic.droid.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseManager;

import java.io.File;

import javax.inject.Inject;

public class DownloadCompleteReceiver extends BroadcastReceiver {

    @Inject
    UserPreferences mUserPrefs;
    @Inject
    DatabaseManager databaseManager;

    public DownloadCompleteReceiver() {
        MainApplication.component().inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                Log.i("downloading", "on receive id" + referenceId);

                DownloadEntity downloadEntity = databaseManager.getDownloadEntityIfExist(referenceId);
                if (downloadEntity != null) {
                    long video_id = downloadEntity.getVideoId();
                    long step_id = downloadEntity.getStepId();
                    databaseManager.deleteDownloadEntityByDownloadId(referenceId);
                    File downloadFolderAndFile = new File(mUserPrefs.getDownloadFolder(), video_id + "");
                    String path = Uri.fromFile(downloadFolderAndFile).getPath();
                    CachedVideo cachedVideo = new CachedVideo(step_id, video_id, path, null);
                    databaseManager.addVideo(cachedVideo);
                }
                return null;
            }
        };
        task.execute();
    }

}
