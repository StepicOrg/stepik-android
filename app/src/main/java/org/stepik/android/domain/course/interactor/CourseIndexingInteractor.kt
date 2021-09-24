package org.stepik.android.domain.course.interactor

import com.google.firebase.appindexing.Action
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.FirebaseUserActions
import com.google.firebase.appindexing.builders.Actions
import com.google.firebase.appindexing.builders.Indexables
import org.stepic.droid.configuration.EndpointResolver
import org.stepic.droid.util.StringUtil

import org.stepik.android.model.Course
import javax.inject.Inject

class CourseIndexingInteractor
@Inject
constructor(
    private val endpointResolver: EndpointResolver,
    private val firebaseAppIndex: FirebaseAppIndex,
    private val firebaseUserActions: FirebaseUserActions
) {
    private var action: Action? = null

    fun startIndexing(course: Course) {
        val title = course.title ?: return
        val uri = StringUtil.getUriForCourse(endpointResolver.getBaseUrl(), course.slug ?: return)

        firebaseAppIndex.update(Indexables.newSimple(title, uri))

        action = Actions.newView(title, uri)
        action?.let(firebaseUserActions::start)
    }

    fun endIndexing() {
        action?.let(firebaseUserActions::end)
        action = null
    }
}