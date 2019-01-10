package org.stepic.droid.core.joining

import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.joining.contract.JoiningListener
import org.stepic.droid.core.joining.contract.JoiningPoster
import org.stepik.android.model.Course
import javax.inject.Inject

class JoiningPosterImpl
@Inject
constructor(
    private val listenerContainer: ListenerContainer<JoiningListener>,
    private val mainHandler: MainHandler
) : JoiningPoster {

    override fun joinCourse(joiningCourse: Course) {
        mainHandler.post {
            listenerContainer.asIterable().forEach {
                it.onSuccessJoin(joiningCourse)
            }
        }
    }
}

