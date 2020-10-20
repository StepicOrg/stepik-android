package org.stepik.android.presentation.course_list

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.base.DataSourceType
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
import org.stepik.android.presentation.course_list.mapper.CourseListQueryStateMapper
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.course_list.UserCoursesOperationBus
import ru.nobird.android.core.model.cast
import ru.nobird.android.core.model.safeCast
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class CourseListQueryPresenter
@Inject
constructor(
    private val courseListQueryStateMapper: CourseListQueryStateMapper,
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

        paginationDisposable += Observable
            .fromArray(SourceTypeComposition.CACHE, SourceTypeComposition.REMOTE)
            .concatMapMaybe { source ->
                courseListInteractor
                    .getCourseListItems(courseListQuery, sourceTypeComposition = source, isAllowFallback = false)
                    .map { courseListItems ->
                        courseListItems to source
                    }
                    .filter { (items, source) -> items.isNotEmpty() || source == SourceTypeComposition.REMOTE }
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
            }
            .subscribeBy(
                onNext = { (items, source) ->
                    val isNeedLoadNextPage = courseListQueryStateMapper.isNeedLoadNextPage(state)

                    state = courseListQueryStateMapper.mapToCourseListLoadedSuccess(courseListQuery, items, source)

                    if (isNeedLoadNextPage) {
                        fetchNextPage()
                    }
                },
                onError = {
                    when (val oldState = state.safeCast<CourseListQueryView.State.Data>()?.courseListViewState) {
                        is CourseListView.State.Content -> {
                            state = state.cast<CourseListQueryView.State.Data>()
                                .copy(
                                    courseListViewState = oldState.copy(oldState.courseListDataItems, oldState.courseListDataItems),
                                    sourceType = null
                                ) // remove fake item in case of error
                            view?.showNetworkError()
                        }
                        else ->
                            state = CourseListQueryView.State.Data(courseListQuery, CourseListView.State.NetworkError, sourceType = null)
                    }
                }
            )
    }

    fun fetchNextPage() {
        val oldState = (state as? CourseListQueryView.State.Data)
            ?: return

        val oldCourseListState = oldState.courseListViewState as? CourseListView.State.Content
            ?: return

        if (oldCourseListState.courseListItems.last() is CourseListItem.PlaceHolder ||
            !oldCourseListState.courseListDataItems.hasNext && oldState.sourceType == DataSourceType.REMOTE
        ) {
            return
        }

        val nextPage =
            if (oldState.sourceType == DataSourceType.REMOTE) {
                oldCourseListState.courseListDataItems.page + 1
            } else {
                1
            }

        val courseListQuery = oldState.courseListQuery.copy(page = nextPage)

        state = oldState.copy(courseListViewState = courseListStateMapper.mapToLoadMoreState(oldCourseListState))
        if (oldState.sourceType != DataSourceType.CACHE) {
            paginationDisposable += courseListInteractor
                .getCourseListItems(courseListQuery, isAllowFallback = false)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = {
                        if (oldState.sourceType == null) {
                            state = oldState.copy(courseListViewState = CourseListView.State.Content(it, it), sourceType = DataSourceType.REMOTE)
                            fetchNextPage()
                        } else {
                            state = oldState.copy(courseListViewState = courseListStateMapper.mapFromLoadMoreToSuccess(oldCourseListState, it))
                        }
                    },
                    onError = {
                        state = oldState.copy(courseListViewState = courseListStateMapper.mapFromLoadMoreToError(oldCourseListState))
                        view?.showNetworkError()
                    }
                )
        }
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
            .getCourseListItems(listOf(course.id), courseViewSource = CourseViewSource.Query(oldState.courseListQuery), sourceTypeComposition = SourceTypeComposition.CACHE)
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