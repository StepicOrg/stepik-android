package org.stepic.droid.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;

import com.squareup.otto.Bus;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.concurrency.SingleThreadExecutor;
import org.stepic.droid.events.video.VideoCachedOnDiskEvent;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.CancelSniffer;
import org.stepic.droid.store.IStoreStateManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.RWLocks;
import org.stepic.droid.util.StorageUtil;

import java.io.File;

import javax.inject.Inject;

public class DownloadCompleteReceiver extends BroadcastReceiver {

    @Inject
    UserPreferences userPreferences;
    @Inject
    DatabaseFacade databaseFacade;
    @Inject
    IStoreStateManager storeStateManager;
    @Inject
    Bus bus;

    @Inject
    CancelSniffer cancelSniffer;

    @Inject
    SingleThreadExecutor threadSingleThreadExecutor;

    @Inject
    DownloadManager downloadManager;

    @Inject
    Analytic analytic;

    public DownloadCompleteReceiver() {
        MainApplication.component().inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        threadSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                blockForInBackground(referenceId);
            }
        });
    }

    private void blockForInBackground(final long referenceId) {
        try {
            RWLocks.DownloadLock.writeLock().lock();

            DownloadEntity downloadEntity = databaseFacade.getDownloadEntityIfExist(referenceId);
            if (downloadEntity != null) {
                final long video_id = downloadEntity.getVideoId();
                final long step_id = downloadEntity.getStepId();
                databaseFacade.deleteDownloadEntityByDownloadId(referenceId);


                if (cancelSniffer.isStepIdCanceled(step_id)) {
                    downloadManager.remove(referenceId);//remove notification (is it really work and need?)
                    cancelSniffer.removeStepIdCancel(step_id);
                    Step step = databaseFacade.getStepById(step_id);
                    if (step != null) {
                        Lesson lesson = databaseFacade.getLessonById(step.getLesson());
                        if (lesson != null) {
                            Unit unit = databaseFacade.getUnitByLessonId(lesson.getId());
                            if (unit != null && cancelSniffer.isUnitIdIsCanceled(unit.getId())) {
                                storeStateManager.updateUnitLessonAfterDeleting(lesson.getId());//automatically update section
                                cancelSniffer.removeUnitIdToCancel(unit.getId());
                                if (cancelSniffer.isSectionIdIsCanceled(unit.getSection())) {
                                    cancelSniffer.removeSectionIdCancel(unit.getSection());
                                }
                            }
                        }
                    }
                } else {
                    //is not canceled

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
                    databaseFacade.addVideo(cachedVideo);

                    final Step step = databaseFacade.getStepById(step_id);
                    step.set_cached(true);
                    step.set_loading(false);
                    databaseFacade.updateOnlyCachedLoadingStep(step);
                    storeStateManager.updateUnitLessonState(step.getLesson());
                    final Lesson lesson = databaseFacade.getLessonById(step.getLesson());
                    Handler mainHandler = new Handler(MainApplication.getAppContext().getMainLooper());
                    //Say to ui that ui is cached now
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (lesson != null)
                                bus.post(new VideoCachedOnDiskEvent(step_id, lesson, cachedVideo));
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
        } finally

        {
            RWLocks.DownloadLock.writeLock().unlock();
        }
    }

}