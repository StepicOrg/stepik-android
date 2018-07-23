package org.stepic.droid.core.presenters

import android.support.annotation.WorkerThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.CourseDetailAnalyticView
import org.stepic.droid.di.course.CourseAndSectionsScope
import org.stepik.android.model.Course
import org.stepic.droid.preferences.SharedPreferenceHelper
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@CourseAndSectionsScope
class CourseDetailAnalyticPresenter
@Inject constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val analytic: Analytic,
        private val sharedPreferenceHelper: SharedPreferenceHelper) : PresenterBase<CourseDetailAnalyticView>() {

    private val isHandling = AtomicBoolean(false)
    private var isCourseHandled = false

    fun onCourseDetailOpened(course: Course) {
        if (isHandling.compareAndSet(false, true)) {
            threadPoolExecutor.execute {
                try {
                    reportAnalytic(course)
                } finally {
                    isHandling.set(false)
                }
            }
        }
    }

    @WorkerThread
    private fun reportAnalytic(course: Course) {
        if (!isCourseHandled) {
            val isAnonymous = sharedPreferenceHelper.authResponseFromStore == null

            if (isAnonymous) {
                analytic.reportEvent(Analytic.CourseDetailScreen.ANONYMOUS)
            } else {
                if (course.enrollment > 0) {
                    analytic.reportEvent(Analytic.CourseDetailScreen.ENROLLED)
                } else {
                    analytic.reportEvent(Analytic.CourseDetailScreen.NOT_ENROLLED)
                }
            }
            isCourseHandled = true
        }
    }

}
