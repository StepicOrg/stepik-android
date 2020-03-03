package org.stepik.android.presentation.course_list

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.presentation.course_list.delegate.CourseListPresenterDelegate
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import timber.log.Timber
import javax.inject.Inject

class CourseListPlaygroundPresenter
@Inject
constructor(
    viewContainer: PresenterViewContainer<CourseListPlaygroundView>,

    private val courseListPresenterDelegate: CourseListPresenterDelegate,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CourseListPlaygroundView>(viewContainer), CourseListPresenterDelegate by courseListPresenterDelegate {

    private var state: CourseListView.State = CourseListView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    fun fetchCourses(vararg courseIds: Long) {
        compositeDisposable += courseListPresenterDelegate
            .onCourseIds(*courseIds)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    state = it
                },
                onError = {
                    Timber.d("Error: $it")
                }
            )
    }
}