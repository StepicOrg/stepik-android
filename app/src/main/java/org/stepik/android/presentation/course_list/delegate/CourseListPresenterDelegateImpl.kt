package org.stepik.android.presentation.course_list.delegate

import io.reactivex.Scheduler
import io.reactivex.Single
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.presentation.course_list.CourseListView
import ru.nobird.android.presentation.base.ViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class CourseListPresenterDelegateImpl
@Inject
constructor(
//    private val stateContainer: StateContainer<CourseListView.State>,
    private val viewContainer: ViewContainer<out CourseListView>,
    private val courseListInteractor: CourseListInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterDelegate<CourseListView>(), CourseListPresenterDelegate {
    override fun onCourseListQuery(courseListQuery: CourseListQuery): Single<CourseListView.State> =
        obtainCoursesListItems(courseListInteractor.getCourseListItems(courseListQuery))

    override fun onCourseIds(vararg courseIds: Long): Single<CourseListView.State> =
        obtainCoursesListItems(courseListInteractor.getCourseListItems(*courseIds))

    private fun obtainCoursesListItems(coursesSource: Single<PagedList<CourseListItem>>): Single<CourseListView.State> =
        coursesSource
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .map { CourseListView.State.Content(it) }

                /*
            .subscribeBy(
                onSuccess = {
//                    stateContainer.state = CourseListView.State.Content(courses = it)
//                    viewContainer.view?.setState(CourseListView.State.Content(courses = it))
                },
                onError = { Timber.d("Error: $it") }
            )
                 */
}