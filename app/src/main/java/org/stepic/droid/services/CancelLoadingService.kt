package org.stepic.droid.services

import android.app.DownloadManager
import android.app.IntentService
import android.app.Service
import android.content.Intent
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
                val section = intent?.getSerializableExtra(AppConstants.KEY_SECTION_BUNDLE) as? Section
                cancelSection(section)
            }
            LoadService.LoadTypeKey.UnitLesson -> {
                val lesson = intent?.getSerializableExtra(AppConstants.KEY_LESSON_BUNDLE) as? Lesson
                cancelUnitLesson(lesson)
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

    @Deprecated("this method is not tested and may be not worked, use cancelStepVideo instead")
    private fun cancelUnitLesson(lesson: Lesson?) {
        lesson?.let {
            try {
                val lessonSteps = lesson.steps
                lessonSteps?.forEach { mCancelSniffer.addStepIdCancel(it) }

                RWLocks.DownloadLock.writeLock().lock()
                val steps = mDb.getStepsOfLesson(lesson.id)
                val downloads = mDb.getAllDownloadEntities()


                //todo: improve time of operation
                for (step in steps) {
                    for (download in downloads) {
                        if (step != null && download != null && download.stepId.equals(step.id)) {
                            val numberOfRemoved = mSystemDownloadManager.remove(download.downloadId)
                            if (numberOfRemoved > 0) {
                                mCancelSniffer.removeStepIdCancel(step.id)
                                mDb.deleteDownloadEntityByDownloadId(download.downloadId)
                                mDb.deleteVideo(download.videoId)
                                if (mDb.isStepCached(step)) {
                                    step.is_cached = false
                                    step.is_loading = false
                                    mDb.updateOnlyCachedLoadingStep(step)
                                }
                            }
                        }
                    }
                }

                mStoreStateManager.updateUnitLessonAfterDeleting(lesson.id)
            } finally {
                RWLocks.DownloadLock.writeLock().unlock()
            }
        }
    }

    private fun cancelStepVideo(stepId: Long) {
        try {
            RWLocks.DownloadLock.writeLock().lock()
            var downloadEntity = mDb.getDownloadEntityByStepId(stepId)
            downloadEntity?.let {
                val numberOfRemoved = mSystemDownloadManager.remove(downloadEntity.downloadId)
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
                    }
                }
            }
        } finally {
            RWLocks.DownloadLock.writeLock().unlock()
        }
    }

    private fun cancelSection(section: Section?) {
        section?.let {

        }
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}