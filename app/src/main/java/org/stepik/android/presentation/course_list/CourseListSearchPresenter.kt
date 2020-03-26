package org.stepik.android.presentation.course_list

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_list.interactor.CourseListSearchInteractor
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.SearchQuery
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class CourseListSearchPresenter
@Inject
constructor(
    private val courseListStateMapper: CourseListStateMapper,
    private val courseListSearchInteractor: CourseListSearchInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,

    viewContainer: PresenterViewContainer<CourseListView>,
    continueCoursePresenterDelegate: CourseContinuePresenterDelegateImpl
) : PresenterBase<CourseListView>(viewContainer), CourseContinuePresenterDelegate by continueCoursePresenterDelegate {

    override val delegates: List<PresenterDelegate<in CourseListView>> =
        listOf(continueCoursePresenterDelegate)

    private var state: CourseListView.State = CourseListView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private var searchQuery: SearchQuery? = null

    private val paginationDisposable = CompositeDisposable()

    override fun attachView(view: CourseListView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchCourses(searchQuery: SearchQuery, forceUpdate: Boolean = false) {
        if (state != CourseListView.State.Idle && !forceUpdate) return

        state = CourseListView.State.Loading
        this.searchQuery = searchQuery

        compositeDisposable += courseListSearchInteractor
            .getCoursesBySearch(searchQuery)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    state = if (it.isNotEmpty()) {
                        CourseListView.State.Content(
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

        val oldSearchQuery = searchQuery ?: return

        if (oldState.courseListItems.last() is CourseListItem.PlaceHolder || !oldState.courseListDataItems.hasNext) {
            return
        }

        val nextPage = oldState.courseListDataItems.page + 1

        state = courseListStateMapper.mapToLoadMoreState(oldState)
        paginationDisposable += courseListSearchInteractor
            .getCoursesBySearch(oldSearchQuery.copy(page = nextPage))
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