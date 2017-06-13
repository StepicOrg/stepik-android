package org.stepic.droid.core.dropping

import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.core.dropping.contract.DroppingListener
import org.stepic.droid.core.dropping.contract.DroppingPoster
import org.stepic.droid.model.Course
import javax.inject.Inject

class DroppingPosterImpl
@Inject constructor(
        private val listenerContainer: ListenerContainer<DroppingListener>)
    : DroppingPoster {


    override fun failDropCourse(course: Course) {
        listenerContainer.iterator().forEach {
            it.onFailDropCourse(course)
        }
    }

    override fun successDropCourse(course: Course) {
        listenerContainer.iterator().forEach {
            it.onSuccessDropCourse(course)
        }
    }

}

