package org.stepik.android.domain.course.interactor

import com.google.firebase.appindexing.Action
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.FirebaseUserActions
import com.google.firebase.appindexing.builders.Actions
import com.google.firebase.appindexing.builders.Indexables
import org.stepic.droid.configuration.Config
import org.stepic.droid.util.StringUtil

import org.stepik.android.model.Course
import javax.inject.Inject

class CourseIndexingInteractor
@Inject
constructor(
    private val config: Config
) {
    private var action: Action? = null

    fun startIndexing(course: Course) {
        val title = course.title ?: return
        val uri = StringUtil.getUriForCourse(config.baseUrl, course.slug ?: return)

        FirebaseAppIndex.getInstance().update(Indexables.newSimple(title, uri))

        action = Actions.newView(title, uri)
        action?.let(FirebaseUserActions.getInstance()::start)
    }

    fun endIndexing() {
        action?.let(FirebaseUserActions.getInstance()::end)
        action = null
    }
}