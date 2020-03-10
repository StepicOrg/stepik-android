package org.stepik.android.presentation.course_list

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

class CourseListPresenter
@Inject
constructor(
    private val courseListStateMapper: CourseListStateMapper,
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
                        CourseListView.State.Content(
                            courseListQuery = courseListQuery,
                            courseListDataItems = it,
                            courseListItems = it
                        )
                    } else {
                        CourseListView.State.Empty
                    }
                },
                onError = {
                    state = CourseListView.State.NetworkError
                }
            )
    }

    fun fetchNextPage() {
        val oldState = state as? CourseListView.State.Content
            ?: return

        val nextPage = oldState.courseListDataItems.page + 1

        val courseListQuery = oldState.courseListQuery.copy(page = nextPage)

        state = courseListStateMapper.mapToLoadMoreState(oldState)
        paginationDisposable += courseListInteractor
            .getCourseListItems(courseListQuery)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    state = courseListStateMapper.mapFromLoadMoreToSuccess(state, it)
                },
                onError = {
                    state = courseListStateMapper.mapFromLoadMoreToError(state)
                    view?.showNetworkError()
                }
            )
    }
}