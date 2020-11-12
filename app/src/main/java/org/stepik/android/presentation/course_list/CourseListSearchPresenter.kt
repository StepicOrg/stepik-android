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
import org.stepik.android.domain.course.model.SourceTypeComposition
import org.stepik.android.domain.course_list.interactor.CourseListSearchInteractor
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.presentation.filter.FilterQueryView
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.course_list.UserCoursesOperationBus
import ru.nobird.android.core.model.cast
import ru.nobird.android.core.model.safeCast
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
    @UserCoursesOperationBus
    private val userCourseOperationObservable: Observable<UserCourse>,

    viewContainer: PresenterViewContainer<CourseListSearchResultView>,
    continueCoursePresenterDelegate: CourseContinuePresenterDelegateImpl
) : PresenterBase<CourseListSearchResultView>(viewContainer), CourseContinuePresenterDelegate by continueCoursePresenterDelegate {

    override val delegates: List<PresenterDelegate<in CourseListSearchResultView>> =
        listOf(continueCoursePresenterDelegate)

    private var state: CourseListSearchResultView.State = CourseListSearchResultView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private val paginationDisposable = CompositeDisposable()

    init {
        compositeDisposable += paginationDisposable
        subscribeForEnrollmentUpdates()
        subscribeForUserCourseOperationUpdates()
    }

    override fun attachView(view: CourseListSearchResultView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchCourses(searchResultQuery: SearchResultQuery, forceUpdate: Boolean = false) {
        if (state != CourseListSearchResultView.State.Idle && !forceUpdate) return

        paginationDisposable.clear()

        logEvent(searchResultQuery)

        state = CourseListSearchResultView.State.Data(
            searchResultQuery = searchResultQuery,
            courseListViewState = CourseListView.State.Loading
        )

        paginationDisposable += courseListSearchInteractor
            .getCoursesBySearch(searchResultQuery)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { (items, source) ->
                    val courseListState = if (source == DataSourceType.CACHE) {
                        if (items.isNotEmpty()) {
                            CourseListView.State.Content(
                                courseListDataItems = items,
                                courseListItems = items
                            )
                        } else {
                            CourseListView.State.Empty
                        }
                    } else {
                        courseListStateMapper.mergeWithUpdatedItems((state as CourseListSearchResultView.State.Data).courseListViewState, items.associateBy { it.course.id })
                    }
                    state = CourseListSearchResultView.State.Data(searchResultQuery, courseListState)
                },
                onError = {
                    when (val oldState = state.safeCast<CourseListSearchResultView.State.Data>()?.courseListViewState) {
                        is CourseListView.State.Content -> {
                            state = state.cast<CourseListSearchResultView.State.Data>()
                                .copy(
                                    courseListViewState = oldState.copy(oldState.courseListDataItems, oldState.courseListDataItems)
                                )
                            view?.showNetworkError()
                        }
                        else ->
                            state = CourseListSearchResultView.State.Data(searchResultQuery, CourseListView.State.NetworkError)
                    }
                }
            )
    }

    fun fetchNextPage() {
        val oldState = state as? CourseListSearchResultView.State.Data
            ?: return

        val oldCourseListState = oldState.courseListViewState as? CourseListView.State.Content
            ?: return

        if (oldCourseListState.courseListItems.last() is CourseListItem.PlaceHolder || !oldCourseListState.courseListDataItems.hasNext) {
            return
        }

        val nextPage = oldCourseListState.courseListDataItems.page + 1

        state = oldState.copy(courseListViewState = courseListStateMapper.mapToLoadMoreState(oldCourseListState))
        paginationDisposable += courseListSearchInteractor
            .getCoursesBySearch(oldState.searchResultQuery.copy(page = nextPage))
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { (items, source) ->
                    state =
                        if (source == DataSourceType.CACHE) {
                            oldState.copy(courseListViewState = courseListStateMapper.mapFromLoadMoreToSuccess(state.cast<CourseListSearchResultView.State.Data>().courseListViewState, items))
                        } else {
                            oldState.copy(courseListViewState = courseListStateMapper.mergeWithUpdatedItems(state.cast<CourseListSearchResultView.State.Data>().courseListViewState, items.associateBy { it.course.id }))
                        }
                },
                onError = {
                    state = oldState.copy(courseListViewState = courseListStateMapper.mapFromLoadMoreToError(oldCourseListState))
                    view?.showNetworkError()
                }
            )
    }

    fun onFilterMenuItemClicked() {
        val oldState = (state as? CourseListSearchResultView.State.Data)
            ?.takeIf { it.courseListViewState is CourseListView.State.Content }
            ?: return

        val filterView = (view as? FilterQueryView)
            ?: return

        filterView.showFilterDialog(filterQuery = oldState.searchResultQuery.filterQuery ?: CourseListFilterQuery())
    }

    /**
     * Enrollment updates
     */
    private fun subscribeForEnrollmentUpdates() {
        compositeDisposable += enrollmentUpdatesObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { enrolledCourse ->
                    val oldState = (state as? CourseListSearchResultView.State.Data)
                        ?: return@subscribeBy

                    state = oldState.copy(courseListViewState = courseListStateMapper.mapToEnrollmentUpdateState(oldState.courseListViewState, enrolledCourse))
                    fetchForEnrollmentUpdate(enrolledCourse)
                },
                onError = emptyOnErrorStub
            )
    }

    private fun fetchForEnrollmentUpdate(course: Course) {
        val oldState = (state as? CourseListSearchResultView.State.Data)
            ?: return

        compositeDisposable += courseListSearchInteractor
            .getCourseListItems(listOf(course.id), courseViewSource = CourseViewSource.Search(oldState.searchResultQuery), sourceTypeComposition = SourceTypeComposition.CACHE)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { courses ->
                    state = oldState.copy(courseListViewState = courseListStateMapper.mapToEnrollmentUpdateState(oldState.courseListViewState, courses.first()))
                },
                onError = emptyOnErrorStub
            )
    }

    /**
     * UserCourse updates
     */
    private fun subscribeForUserCourseOperationUpdates() {
        compositeDisposable += userCourseOperationObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { userCourse ->
                    val oldState = (state as? CourseListSearchResultView.State.Data)
                        ?: return@subscribeBy

                    state = oldState.copy(courseListViewState = courseListStateMapper.mapToUserCourseUpdate(oldState.courseListViewState, userCourse))
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