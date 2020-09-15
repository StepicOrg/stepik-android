package org.stepik.android.presentation.course_list

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course.model.SourceTypeComposition
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Course
import org.stepik.android.presentation.catalog.model.CatalogItem
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.course_list.UserCoursesOperationBus
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class CourseListQueryPresenter
@Inject
constructor(
    private val courseListStateMapper: CourseListStateMapper,
    private val courseListInteractor: CourseListInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    @EnrollmentCourseUpdates
    private val enrollmentUpdatesObservable: Observable<Course>,
    @UserCoursesOperationBus
    private val userCourseOperationObservable: Observable<UserCourse>,

    viewContainer: PresenterViewContainer<CourseListQueryView>,
    continueCoursePresenterDelegate: CourseContinuePresenterDelegateImpl
) : PresenterBase<CourseListQueryView>(viewContainer),
    CourseContinuePresenterDelegate by continueCoursePresenterDelegate,
    CatalogItem {

    override val delegates: List<PresenterDelegate<in CourseListQueryView>> =
        listOf(continueCoursePresenterDelegate)

    private var state: CourseListQueryView.State = CourseListQueryView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private var isFromCache: Boolean = false

    var firstVisibleItemPosition: Int? = null

    private val paginationDisposable = CompositeDisposable()

    init {
        compositeDisposable += paginationDisposable
        subscribeForEnrollmentUpdates()
        subscribeForUserCourseOperationUpdates()
    }

    override fun attachView(view: CourseListQueryView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchCourses(courseListQuery: CourseListQuery, forceUpdate: Boolean = false) {
        if (state != CourseListQueryView.State.Idle && !forceUpdate) return

        paginationDisposable.clear()

        state = CourseListQueryView.State.Data(
            courseListQuery = courseListQuery,
            courseListViewState = CourseListView.State.Loading
        )

        paginationDisposable += Single
            .concat(
                courseListInteractor
                    .getCourseListItems(courseListQuery, sourceTypeComposition = SourceTypeComposition.CACHE)
                    .map { courseListItems ->
                        courseListItems to SourceTypeComposition.CACHE
                    },

                courseListInteractor
                    .getCourseListItems(courseListQuery, sourceTypeComposition = SourceTypeComposition.REMOTE)
                    .map { courseListItems ->
                        courseListItems to SourceTypeComposition.REMOTE
                    }
            )
            .toObservable()
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { (items, source) ->
                    state = resolveInitialFetchCourses(items, source)
                    isFromCache = source == SourceTypeComposition.CACHE || isSameCourseListItems(state, items)
                },
                onError = {
                    when (val oldState = (state as? CourseListQueryView.State.Data)?.courseListViewState) {
                        is CourseListView.State.Content -> {
                            state = (state as CourseListQueryView.State.Data).copy(courseListViewState = oldState)
                            view?.showNetworkError()
                        }
                        else ->
                            state = CourseListQueryView.State.Data(courseListQuery, CourseListView.State.NetworkError)
                    }
                }
            )
    }

    private fun isSameCourseListItems(courseListQueryState: CourseListQueryView.State, items: PagedList<CourseListItem.Data>): Boolean {
        val oldState = (courseListQueryState as? CourseListQueryView.State.Data)
            ?: return false

        val oldCourseListState = (oldState.courseListViewState as? CourseListView.State.Content)
            ?: return false

        if (oldCourseListState.courseListDataItems.size != items.size) {
            return false
        }

        return oldCourseListState.courseListDataItems.zip(items).all { (firstListItem, secondListItem)  -> firstListItem.id == secondListItem.id }
    }

    private fun isMustStopFetchNextPage(oldCourseListState: CourseListView.State.Content): Boolean =
        if (isFromCache && oldCourseListState.courseListItems.last() !is CourseListItem.PlaceHolder) {
            false
        } else {
            oldCourseListState.courseListItems.last() is CourseListItem.PlaceHolder || !oldCourseListState.courseListDataItems.hasNext
        }

    fun fetchNextPage() {
        val oldState = (state as? CourseListQueryView.State.Data)
            ?: return

        val oldCourseListState = oldState.courseListViewState as? CourseListView.State.Content
            ?: return

        if (isMustStopFetchNextPage(oldCourseListState)) {
            return
        }

        isFromCache = false

        val nextPage = oldCourseListState.courseListDataItems.page + 1

        val courseListQuery = oldState.courseListQuery.copy(page = nextPage)

        state = oldState.copy(courseListViewState = courseListStateMapper.mapToLoadMoreState(oldCourseListState))
        paginationDisposable += courseListInteractor
            .getCourseListItems(courseListQuery, isAllowFallback = false)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    state = oldState.copy(courseListViewState = courseListStateMapper.mapFromLoadMoreToSuccess(oldCourseListState, it))
                },
                onError = {
                    isFromCache = true
                    state = oldState.copy(courseListViewState = courseListStateMapper.mapFromLoadMoreToError(oldCourseListState))
                    view?.showNetworkError()
                }
            )
    }

    private fun resolveInitialFetchCourses(items: PagedList<CourseListItem.Data>, source: SourceTypeComposition): CourseListQueryView.State.Data =
        if (items.isNotEmpty()) {
            val courseListItems = if (source == SourceTypeComposition.CACHE) {
                items + CourseListItem.PlaceHolder()
            } else {
                items
            }
            (state as CourseListQueryView.State.Data).copy(
                courseListViewState = CourseListView.State.Content(
                    courseListDataItems = items,
                    courseListItems = courseListItems
                )
            )
        } else {
            (state as CourseListQueryView.State.Data).copy(
                courseListViewState = CourseListView.State.Empty
            )
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
                    val oldState = (state as? CourseListQueryView.State.Data)
                        ?: return@subscribeBy

                    state = oldState.copy(courseListViewState = courseListStateMapper.mapToEnrollmentUpdateState(oldState.courseListViewState, enrolledCourse))
                    fetchForEnrollmentUpdate(enrolledCourse)
                },
                onError = emptyOnErrorStub
            )
    }

    // TODO Remove duplication here, at CourseListCollectionPresenter.kt and CourseListSearchPresenter.kt in future sprint
    private fun fetchForEnrollmentUpdate(course: Course) {
        val oldState = (state as? CourseListQueryView.State.Data)
            ?: return

        compositeDisposable += courseListInteractor
            .getCourseListItems(course.id, courseViewSource = CourseViewSource.Query(oldState.courseListQuery), sourceTypeComposition = SourceTypeComposition.CACHE)
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
                    val oldState = (state as? CourseListQueryView.State.Data)
                        ?: return@subscribeBy

                    state = oldState.copy(courseListViewState = courseListStateMapper.mapToUserCourseUpdate(oldState.courseListViewState, userCourse))
                },
                onError = emptyOnErrorStub
            )
    }

    public override fun onCleared() {
        super.onCleared()
    }
}