package org.stepic.droid.services

import android.app.DownloadManager
import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.util.Log
import com.squareup.otto.Bus
import org.stepic.droid.base.MainApplication
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Section
import org.stepic.droid.model.Step
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.store.ICancelSniffer
import org.stepic.droid.store.IStoreStateManager
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.RWLocks
import javax.inject.Inject

class CancelLoadingService : IntentService("cancel_loading") {

    @Inject
    lateinit var mSystemDownloadManager: DownloadManager
    @Inject
    lateinit var mUserPrefs: UserPreferences
    @Inject
    lateinit var mBus: Bus
    @Inject
    lateinit var mDb: DatabaseFacade
    @Inject
    lateinit var mStoreStateManager: IStoreStateManager
    @Inject
    lateinit var mCancelSniffer: ICancelSniffer


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
            LoadService.LoadTypeKey.Course -> {
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
            var downloadEntity = mDb.getDownloadEntityByStepId(stepId)
            downloadEntity?.let {
                val numberOfRemoved = mSystemDownloadManager.remove(downloadEntity.downloadId)
                Log.d("ttt", "remove downloadId:step " + downloadEntity.downloadId + ":" + stepId + " Num:" + numberOfRemoved)
                if (numberOfRemoved > 0) {
                    mCancelSniffer.removeStepIdCancel(stepId)
                    mDb.deleteDownloadEntityByDownloadId(downloadEntity.downloadId)
                    mDb.deleteVideo(downloadEntity.videoId)
                    val step = mDb.getStepById(stepId)

                    if (step != null) {
                        step.is_cached = false
                        step.is_loading = false
                        mDb.updateOnlyCachedLoadingStep(step)
                        mStoreStateManager.updateStepAfterDeleting(step)

                        val lesson = mDb.getLessonById(step.id)
                        lesson?.let {
                            val unit = mDb.getUnitByLessonId(lesson.id)
                            unit?.let {
                                if (mCancelSniffer.isUnitIdIsCanceled(unit.id)) {
                                    mCancelSniffer.removeUnitIdCancel(unit.id)

                                    if (mCancelSniffer.isSectionIdIsCanceled(unit.section)) {
                                        mCancelSniffer.removeSectionIdCancel(unit.section)
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