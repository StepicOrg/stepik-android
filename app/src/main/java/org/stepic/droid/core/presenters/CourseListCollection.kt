package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.di.course_list.CourseListScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.web.Api
import javax.inject.Inject

@CourseListScope
class CourseListCollection
@Inject
constructor(
        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler,
        private val api: Api
) : PresenterBase<CoursesView>() {

    companion object {
        //collections are small (<10 courses), so pagination is not needed
        private val DEFAULT_PAGE = 1
    }

    fun onShowCollection(courseIds: LongArray) {
        //todo add progresses and ratings, when it will be implemented.
        view?.showLoading()
        api
                .getCoursesReactive(DEFAULT_PAGE, courseIds)
                .map {
                    val coursesMap = it.courses.associateBy { it.courseId }
                    courseIds
                            .asIterable()
                            .mapNotNull {
                                coursesMap[it]
                            }
                }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    view?.showCourses(it)
                }, {
                    view?.showConnectionProblem()
                })

    }

}
