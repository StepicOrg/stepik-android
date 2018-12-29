package org.stepic.droid.core.dropping

import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.dropping.contract.DroppingListener
import org.stepic.droid.core.dropping.contract.DroppingPoster
import org.stepik.android.model.Course
import javax.inject.Inject

class DroppingPosterImpl
@Inject
constructor(
    private val listenerContainer: ListenerContainer<DroppingListener>,
    private val mainHandler: MainHandler
) : DroppingPoster {
    override fun failDropCourse(course: Course) {
        mainHandler.post {
            listenerContainer.asIterable().forEach {
                it.onFailDropCourse(course)
            }
        }
    }

    override fun successDropCourse(course: Course) {
        mainHandler.post {
            listenerContainer.asIterable().forEach {
                it.onSuccessDropCourse(course)
            }
        }
    }
}

