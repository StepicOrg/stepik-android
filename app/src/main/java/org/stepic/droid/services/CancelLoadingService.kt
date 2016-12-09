package org.stepic.droid.services

import android.app.DownloadManager
import android.app.IntentService
import android.app.Service
import android.content.Intent
import org.stepic.droid.base.MainApplication
import org.stepic.droid.store.ICancelSniffer
import org.stepic.droid.store.IStoreStateManager
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.RWLocks
import javax.inject.Inject

class CancelLoadingService : IntentService("cancel_loading") {

    @Inject
    lateinit var systemDownloadManager: DownloadManager
    @Inject
    lateinit var databaseFacade: DatabaseFacade
    @Inject
    lateinit var storeStateManager: IStoreStateManager
    @Inject
    lateinit var cancelSniffer: ICancelSniffer


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MainApplication.component().inject(this)
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }

    override fun onHandleIntent(intent: Intent?) {
        val type = intent?.getSerializableExtra(AppConstants.KEY_LOAD_TYPE) as? LoadService.LoadTypeKey
        when (type) {
            LoadService.LoadTypeKey.Section -> {
                return
            }
            LoadService.LoadTypeKey.UnitLesson -> {
                return
            }
            LoadService.LoadTypeKey.Step -> {
                val stepId = intent?.getLongExtra(AppConstants.KEY_STEP_BUNDLE, -1)
                if (stepId != null && stepId >= 0L) {
                    cancelStepVideo(stepId)
                }
            }
        }
    }

    private fun cancelStepVideo(stepId: Long) {
        try {
            RWLocks.DownloadLock.writeLock().lock()
            var downloadEntity = databaseFacade.getDownloadEntityByStepId(stepId)
            downloadEntity?.let {
                val numberOfRemoved = systemDownloadManager.remove(downloadEntity.downloadId)
                if (numberOfRemoved > 0) {
                    cancelSniffer.removeStepIdCancel(stepId)
                    databaseFacade.deleteDownloadEntityByDownloadId(downloadEntity.downloadId)
                    databaseFacade.deleteVideo(downloadEntity.videoId)
                    val step = databaseFacade.getStepById(stepId)

                    if (step != null) {
                        step.is_cached = false
                        step.is_loading = false
                        databaseFacade.updateOnlyCachedLoadingStep(step)
                        storeStateManager.updateStepAfterDeleting(step)

                        val lesson = databaseFacade.getLessonById(step.id)
                        lesson?.let {
                            val unit = databaseFacade.getUnitByLessonId(lesson.id)
                            unit?.let {
                                if (cancelSniffer.isUnitIdIsCanceled(unit.id)) {
                                    cancelSniffer.removeUnitIdCancel(unit.id)

                                    if (cancelSniffer.isSectionIdIsCanceled(unit.section)) {
                                        cancelSniffer.removeSectionIdCancel(unit.section)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            RWLocks.DownloadLock.writeLock().unlock()
        }
    }

}