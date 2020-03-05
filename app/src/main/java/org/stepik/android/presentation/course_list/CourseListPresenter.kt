package org.stepik.android.presentation.course_list

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.concatWithPagedList
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListItem
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

    private val paginationDisposable = CompositeDisposable()

    override fun attachView(view: CourseListView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchCourses(courseListQuery: CourseListQuery) {
        if (state != CourseListView.State.Idle) return

        state = CourseListView.State.Loading

        compositeDisposable += courseListInteractor
            .getCourseListItems(courseListQuery)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    state = if (it.isNotEmpty()) {
                        CourseListView.State.Content(courseListQuery = courseListQuery, courseListItems = it)
                    } else {
                        CourseListView.State.Empty
                    }
                },
                onError = {
                    it.printStackTrace()
                    state = CourseListView.State.NetworkError
                }
            )
    }

    fun fetchNextPage() {
        val oldState = state as? CourseListView.State.Content
            ?: return

        val currentItems = oldState.courseListItems

        val nextPage = (currentItems as? PagedList<CourseListItem>)
            ?.page
            ?.plus(1)
            ?: 1

        val courseListQuery = oldState.courseListQuery.copy(page = nextPage)

        state = CourseListView.State.ContentLoading(oldState.courseListItems)
        paginationDisposable += courseListInteractor
            .getCourseListItems(courseListQuery)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    state = CourseListView.State.Content(courseListQuery, currentItems.concatWithPagedList(it))
                },
                onError = { state = oldState; view?.showNetworkError() }
            )
    }
}