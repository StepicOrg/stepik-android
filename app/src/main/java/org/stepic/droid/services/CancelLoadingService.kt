package org.stepic.droid.services

import android.app.DownloadManager
import android.app.IntentService
import android.app.Service
import android.content.Intent
import org.stepic.droid.base.App
import org.stepic.droid.storage.CancelSniffer
import org.stepic.droid.storage.StoreStateManager
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.RWLocks
import org.stepic.droid.util.SuppressFBWarnings
import javax.inject.Inject

class CancelLoadingService : IntentService("cancel_loading") {

    @Inject
    lateinit var systemDownloadManager: DownloadManager
    @Inject
    lateinit var databaseFacade: DatabaseFacade
    @Inject
    lateinit var storeStateManager: StoreStateManager
    @Inject
    lateinit var cancelSniffer: CancelSniffer


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        App.component().inject(this)
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }

    override fun onHandleIntent(intent: Intent?) {
        val type = intent?.getSerializableExtra(AppConstants.KEY_LOAD_TYPE) as? LoadService.LoadTypeKey
        when (type) {
            LoadService.LoadTypeKey.Section -> {
                return
            }
            LoadService.LoadTypeKey.Lesson -> {
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

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private fun cancelStepVideo(stepId: Long) {
        try {
            RWLocks.DownloadLock.writeLock().lock()
            val downloadEntity = databaseFacade.getDownloadEntityByStepId(stepId) ?: return
            val numberOfRemoved = systemDownloadManager.remove(downloadEntity.downloadId)
            if (numberOfRemoved > 0) {
                cancelSniffer.removeStepIdCancel(stepId)
                databaseFacade.deleteDownloadEntityByDownloadId(downloadEntity.downloadId)
                databaseFacade.deleteVideo(downloadEntity.videoId)
                val step = databaseFacade.getStepById(stepId)

                if (step != null) {
                    step.isCached = false
                    step.isLoading = false
                    databaseFacade.updateOnlyCachedLoadingStep(step)
                    storeStateManager.updateStepAfterDeleting(step)

                }
            }
        } finally {
            RWLocks.DownloadLock.writeLock().unlock()
        }
    }

}