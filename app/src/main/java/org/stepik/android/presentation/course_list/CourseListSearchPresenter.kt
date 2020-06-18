package org.stepik.android.presentation.course_list

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.interactor.CourseListSearchInteractor
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class CourseListSearchPresenter
@Inject
constructor(
    private val analytic: Analytic,
    private val courseListStateMapper: CourseListStateMapper,
    private val courseListSearchInteractor: CourseListSearchInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    @EnrollmentCourseUpdates
    private val enrollmentUpdatesObservable: Observable<Course>,

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

    private var searchResultQuery: SearchResultQuery? = null

    private val paginationDisposable = CompositeDisposable()

    init {
        compositeDisposable += paginationDisposable
        subscribeForEnrollmentUpdates()
    }

    override fun attachView(view: CourseListView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchCourses(searchResultQuery: SearchResultQuery, forceUpdate: Boolean = false) {
        if (state != CourseListView.State.Idle && !forceUpdate) return

        paginationDisposable.clear()

        val oldState = state

        logEvent(searchResultQuery)

        state = CourseListView.State.Loading
        this.searchResultQuery = searchResultQuery

        paginationDisposable += courseListSearchInteractor
            .getCoursesBySearch(searchResultQuery)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { (items, source) ->
                    state =
                        if (source == DataSourceType.CACHE) {
                            if (items.isNotEmpty()) {
                                CourseListView.State.Content(
                                    courseListDataItems = items,
                                    courseListItems = items
                                )
                            } else {
                                CourseListView.State.Empty
                            }
                        } else {
                            courseListStateMapper.mergeWithUpdatedItems(state, items.associateBy { it.course.id })
                        }
                },
                onError = {
                    when (oldState) {
                        is CourseListView.State.Content -> {
                            state = oldState
                            view?.showNetworkError()
                        }
                        else ->
                            state = CourseListView.State.NetworkError
                    }
                }
            )
    }

    fun fetchNextPage() {
        val oldState = state as? CourseListView.State.Content
            ?: return

        val oldSearchQuery = searchResultQuery ?: return

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
                onNext = { (items, source) ->
                    state =
                        if (source == DataSourceType.CACHE) {
                            courseListStateMapper.mapFromLoadMoreToSuccess(state, items)
                        } else {
                            courseListStateMapper.mergeWithUpdatedItems(state, items.associateBy { it.course.id })
                        }
                },
                onError = {
                    state = courseListStateMapper.mapFromLoadMoreToError(state)
                    view?.showNetworkError()
                }
            )
    }

    private fun subscribeForEnrollmentUpdates() {
        compositeDisposable += enrollmentUpdatesObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { enrolledCourse ->
                    state = courseListStateMapper.mapToEnrollmentUpdateState(state, enrolledCourse)
                    fetchForEnrollmentUpdate(enrolledCourse)
                },
                onError = emptyOnErrorStub
            )
    }

    private fun fetchForEnrollmentUpdate(course: Course) {
        val oldSearchQuery = searchResultQuery ?: return

        compositeDisposable += courseListSearchInteractor
            .getCourseListItems(course.id, courseViewSource = CourseViewSource.Search(oldSearchQuery))
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { courses ->
                    state = courseListStateMapper.mapToEnrollmentUpdateState(state, courses.first())
                },
                onError = emptyOnErrorStub
            )
    }

    private fun logEvent(searchResultQuery: SearchResultQuery) {
        if (searchResultQuery.query == null) {
            analytic.reportEvent(Analytic.Search.SEARCH_NULL)
        } else {
            analytic.reportEventWithName(Analytic.Search.SEARCH_QUERY, searchResultQuery.query)
        }
    }
}