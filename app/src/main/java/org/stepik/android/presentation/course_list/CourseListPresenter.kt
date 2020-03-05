package org.stepik.android.presentation.course_list

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListQuery
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

class CourseListPresenter
@Inject
constructor(
    private val courseListInteractor: CourseListInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CourseListView>() {

    private var state: CourseListView.State = CourseListView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    fun fetchCourses(courseListQuery: CourseListQuery) {
        compositeDisposable += courseListInteractor
            .getCourseListItems(courseListQuery)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                },
                onError = {
                }
            )
    }
}