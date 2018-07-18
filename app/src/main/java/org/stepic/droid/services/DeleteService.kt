package org.stepic.droid.services

import android.app.DownloadManager
import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.Handler
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.downloads.contract.DownloadsPoster
import org.stepik.android.model.structure.Lesson
import org.stepik.android.model.structure.Step
import org.stepic.droid.storage.StoreStateManager
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.SuppressFBWarnings
import java.io.File
import javax.inject.Inject

class DeleteService : IntentService("delete_service") {
    @Inject
    lateinit var systemDownloadManager: DownloadManager
    @Inject
    lateinit var databaseFacade: DatabaseFacade
    @Inject
    lateinit var storeStateManager: StoreStateManager
    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var downloadsPoster: DownloadsPoster

    override fun onCreate() {
        super.onCreate()
        App
                .componentManager()
                .downloadsComponent()
                .inject(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }


    override fun onHandleIntent(intent: Intent) {
        val type = intent.getSerializableExtra(AppConstants.KEY_LOAD_TYPE) as? LoadService.LoadTypeKey
        try {
            when (type) {
                LoadService.LoadTypeKey.Section -> {
                    val sectionId = intent.getLongExtra(AppConstants.KEY_SECTION_BUNDLE, -1).takeIf { it >= 0 } ?: return
                    removeFromDisk(sectionId)
                }
                LoadService.LoadTypeKey.Lesson -> {
                    val lesson = intent.getParcelableExtra<Lesson>(AppConstants.KEY_LESSON_BUNDLE)
                    removeFromDisk(lesson)
                }
                LoadService.LoadTypeKey.Step -> {
                    val step = intent.getSerializableExtra(AppConstants.KEY_STEP_BUNDLE) as? Step
                    removeFromDisk(step)
                }
            }
        } catch (ex: NullPointerException) {
            //possibly user click clear cache;
            //            throw ex;
            analytic.reportError(Analytic.Error.DELETE_SERVICE_ERROR, ex)
            databaseFacade.dropDatabase()
        }

    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private fun removeFromDisk(step: Step?) {
        step?.block?.video?.let {
            val cachedVideo = databaseFacade.getCachedVideoIfExist(it)
            var file = File(cachedVideo?.url)
            if (cachedVideo != null && file.exists()) {
                file.delete()
            }

            //delete png thumbnail
            file = File(cachedVideo?.thumbnail)
            if (cachedVideo != null && file.exists()) {
                file.delete()
            }

            databaseFacade.deleteVideo(it)
        }
        step?.let {
            step.isCached = false
            step.isLoading = false
            databaseFacade.updateOnlyCachedLoadingStep(step)
//            database.deleteStep(step) // remove steps FIXME: MAYBE NOT DELETE STEP?
            storeStateManager.updateStepAfterDeleting(step)
            val mainHandler = Handler(App.getAppContext().mainLooper)
            mainHandler.post {
                downloadsPoster.stepRemoved(step.id)
            }
        }
    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private fun removeFromDisk(lesson: Lesson?) {
        if (lesson == null) {
            return
        }
        val steps = databaseFacade.getStepsOfLesson(lesson.id)
        for (step in steps) {
            removeFromDisk(step)
        }
    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private fun removeFromDisk(sectionId: Long) {
        val units = databaseFacade.getAllUnitsOfSection(sectionId)
        val lessons = units
                .mapNotNull { databaseFacade.getLessonOfUnit(it) }

        for (lesson in lessons) {
            removeFromDisk(lesson)
        }
    }
}
