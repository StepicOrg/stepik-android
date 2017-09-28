package org.stepic.droid.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.concurrency.SingleThreadExecutor;
import org.stepic.droid.core.downloads.contract.DownloadsPoster;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.storage.CancelSniffer;
import org.stepic.droid.storage.StoreStateManager;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.RWLocks;
import org.stepic.droid.util.StorageUtil;

import java.io.File;

import javax.inject.Inject;

import timber.log.Timber;

import static org.stepic.droid.storage.DownloadManagerExtensionKt.getDownloadStatus;

public class DownloadCompleteReceiver extends BroadcastReceiver {
    @Inject
    DownloadManager systemDownloadManager;

    @Inject
    UserPreferences userPreferences;

    @Inject
    DatabaseFacade databaseFacade;

    @Inject
    StoreStateManager storeStateManager;

    @Inject
    CancelSniffer cancelSniffer;

    @Inject
    SingleThreadExecutor threadSingleThreadExecutor;

    @Inject
    DownloadManager downloadManager;

    @Inject
    Analytic analytic;

    @Inject
    DownloadsPoster downloadsPoster;

    public DownloadCompleteReceiver() {
        Timber.d("create DownloadCompleteReceiver");
        App.Companion
                .componentManager()
                .downloadsComponent()
                .inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("onReceive");
        final long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        Timber.d("referenceId = %d", referenceId);

        if (referenceId >= 0) {
            threadSingleThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    blockForInBackground(referenceId);
                }
            });
        }
    }

    private CachedVideo prepareCachedVideo(DownloadEntity downloadEntity) {
        final long video_id = downloadEntity.getVideoId();
        final long step_id = downloadEntity.getStepId();

        File userDownloadFolder = userPreferences.getUserDownloadFolder();
        File downloadFolderAndFile = new File(userDownloadFolder, video_id + "");
        String path = Uri.fromFile(downloadFolderAndFile).getPath();
        String thumbnail = downloadEntity.getThumbnail();
        if (userPreferences.isSdChosen()) {
            File sdFile = userPreferences.getSdCardDownloadFolder();
            if (sdFile != null) {
                try {
                    StorageUtil.moveFile(userDownloadFolder.getPath(), video_id + "", sdFile.getPath());
                    StorageUtil.moveFile(userDownloadFolder.getPath(), video_id + AppConstants.THUMBNAIL_POSTFIX_EXTENSION, sdFile.getPath());
                    downloadFolderAndFile = new File(sdFile, video_id + "");
                    final File thumbnailFile = new File(sdFile, video_id + AppConstants.THUMBNAIL_POSTFIX_EXTENSION);
                    path = Uri.fromFile(downloadFolderAndFile).getPath();
                    thumbnail = Uri.fromFile(thumbnailFile).getPath();
                } catch (Exception er) {
                    analytic.reportError(Analytic.Error.FAIL_TO_MOVE, er);
                }
            }

        }

        final CachedVideo cachedVideo = new CachedVideo(step_id, video_id, path, thumbnail);
        cachedVideo.setQuality(downloadEntity.getQuality());
        return cachedVideo;
    }

    private void blockForInBackground(final long referenceId) {
        try {
            RWLocks.DownloadLock.writeLock().lock();

            final DownloadEntity downloadEntity = databaseFacade.getDownloadEntityIfExist(referenceId);
            if (downloadEntity != null) {
                final long step_id = downloadEntity.getStepId();
                databaseFacade.deleteDownloadEntityByDownloadId(referenceId);


                if (cancelSniffer.isStepIdCanceled(step_id)) {
                    downloadManager.remove(referenceId);//remove notification (is it really work and need?)
                    cancelSniffer.removeStepIdCancel(step_id);
                } else {
                    //is not canceled
                    final Step step = databaseFacade.getStepById(step_id);
                    if (step == null) return;
                    final long status = getDownloadStatus(systemDownloadManager, referenceId);

                    final CachedVideo cachedVideo;
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        cachedVideo = prepareCachedVideo(downloadEntity);
                        databaseFacade.addVideo(cachedVideo);
                        step.set_cached(true);
                    } else {
                        cachedVideo = null;
                        step.set_cached(false);
                    }

                    step.set_loading(false);
                    databaseFacade.updateOnlyCachedLoadingStep(step);
                    storeStateManager.updateUnitLessonState(step.getLesson());

                    final Lesson lesson = databaseFacade.getLessonById(step.getLesson());
                    Handler mainHandler = new Handler(App.Companion.getAppContext().getMainLooper());
                    //Say to ui that ui is cached now
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (lesson != null) {
                                if (cachedVideo != null) {
                                    downloadsPoster.downloadComplete(step_id, lesson, cachedVideo);
                                } else {
                                    final Context context = App.Companion.getAppContext();
                                    Toast.makeText(context, context.getString(R.string.video_download_fail, lesson.getTitle() + " " + downloadEntity.getVideoId()), Toast.LENGTH_SHORT).show();
                                    downloadsPoster.downloadFailed(referenceId);
                                }
                            }
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            } else {
                if (referenceId < 0) {
                    analytic.reportError(Analytic.Error.DOWNLOAD_ID_NEGATIVE, new IllegalArgumentException("ReferenceId was " + referenceId));
                } else {
                    downloadManager.remove(referenceId);//remove notification (is it really work and need?)
                }
            }
        } finally {
            RWLocks.DownloadLock.writeLock().unlock();
        }
    }

}