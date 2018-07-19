package org.stepic.droid.storage

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import org.stepik.android.model.Lesson
import org.stepik.android.model.Step
import org.stepic.droid.services.DeleteService
import org.stepic.droid.services.LoadService
import org.stepic.droid.util.AppConstants
import java.io.Serializable
import javax.inject.Inject

class CleanManagerImpl @Inject constructor(private val context: Context) : CleanManager {

    override fun removeSection(sectionId: Long) {
        val loadIntent = Intent(context, DeleteService::class.java)

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Section)
        loadIntent.putExtra(AppConstants.KEY_SECTION_BUNDLE, sectionId)

        context.startService(loadIntent)
    }


    override fun removeLesson(lesson: Lesson?) {
        if (lesson == null) {
            return
        }

        val loadIntent = Intent(context, DeleteService::class.java)

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Lesson)
        loadIntent.putExtra(AppConstants.KEY_LESSON_BUNDLE, lesson as Parcelable)

        context.startService(loadIntent)

    }

    override fun removeStep(step: Step?) {
        if (step == null) {
            return
        }

        val loadIntent = Intent(context, DeleteService::class.java)

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Step)
        loadIntent.putExtra(AppConstants.KEY_STEP_BUNDLE, step as Serializable)

        context.startService(loadIntent)
    }


}
