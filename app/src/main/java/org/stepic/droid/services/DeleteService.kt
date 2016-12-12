package org.stepic.droid.services

import android.app.DownloadManager
import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.Handler
import com.squareup.otto.Bus
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.MainApplication
import org.stepic.droid.events.steps.StepRemovedEvent
import org.stepic.droid.model.*
import org.stepic.droid.model.Unit
import org.stepic.droid.store.IStoreStateManager
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import java.io.File
import java.util.*
import javax.inject.Inject

class DeleteService : IntentService("delete_service") {
    @Inject
    lateinit var systemDownloadManager: DownloadManager
    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var databaseFacade: DatabaseFacade
    @Inject
    lateinit var storeStateManager: IStoreStateManager
    @Inject
    lateinit var analytic: Analytic

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MainApplication.component().inject(this)
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }


    override fun onHandleIntent(intent: Intent) {
        val type = intent.getSerializableExtra(AppConstants.KEY_LOAD_TYPE) as? LoadService.LoadTypeKey
        try {
            when (type) {
                LoadService.LoadTypeKey.Section -> {
                    val section = intent.getSerializableExtra(AppConstants.KEY_SECTION_BUNDLE) as? Section
                    removeFromDisk(section)
                }
                LoadService.LoadTypeKey.UnitLesson -> {
                    val unit = intent.getSerializableExtra(AppConstants.KEY_UNIT_BUNDLE) as? Unit
                    val lesson = intent.getSerializableExtra(AppConstants.KEY_LESSON_BUNDLE) as? Lesson
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

    private fun removeFromDisk(step: Step?) {
        step?.block?.video?.let {
            val path = databaseFacade.getPathToVideoIfExist(it)
            var file = File(path)
            if (file.exists()) {
                file.delete()
            }

            //delete png thumbnail
            file = File(path + AppConstants.THUMBNAIL_POSTFIX_EXTENSION)
            if (file.exists()) {
                file.delete()
            }

            databaseFacade.deleteVideo(it)
        }
        step?.let {
            step.is_cached = false
            step.is_loading = false
            databaseFacade.updateOnlyCachedLoadingStep(step)
//            database.deleteStep(step) // remove steps FIXME: MAYBE NOT DELETE STEP?
            storeStateManager.updateStepAfterDeleting(step)
            val mainHandler = Handler(MainApplication.getAppContext().mainLooper)
            mainHandler.post { bus.post(StepRemovedEvent(step.id)) }
        }
    }

    private fun removeFromDisk(lesson: Lesson?) {
        lesson?.let {
            val steps = databaseFacade.getStepsOfLesson(lesson.id)
            for (step in steps) {
                removeFromDisk(step)
            }
        }
    }

    private fun removeFromDisk(section: Section?) {
        section?.let {
            val units = databaseFacade.getAllUnitsOfSection(section.id)
            val lessons = ArrayList<Lesson>()
            for (unit in units) {
                val lesson = databaseFacade.getLessonOfUnit(unit)
                if (lesson != null) {
                    lessons.add(lesson)
                }
            }

            for (lesson in lessons) {
                removeFromDisk(lesson)
            }
        }
    }

    private fun removeFromDisk(course: Course?) {
        course?.let {
            val sections = databaseFacade.getAllSectionsOfCourse(course)
            for (section in sections) {
                removeFromDisk(section)
            }
        }
    }
}
