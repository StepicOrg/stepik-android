package org.stepik.android.presentation.course_list

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.interactor.CourseListUserInteractor
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.course_list.model.UserCoursesLoaded
import org.stepik.android.domain.personal_deadlines.interactor.DeadlinesSynchronizationInteractor
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.presentation.course_list.mapper.CourseListUserStateMapper
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.course_list.UserCoursesLoadedBus
import org.stepik.android.view.injection.course_list.UserCoursesOperationBus
import org.stepik.android.view.injection.course_list.UserCoursesUpdateBus
import retrofit2.HttpException
import ru.nobird.android.core.model.slice
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class CourseListUserPresenter
@Inject
constructor(
    private val analytic: Analytic,
    private val courseListStateMapper: CourseListStateMapper,
    private val courseListUserStateMapper: CourseListUserStateMapper,
    private val courseListUserInteractor: CourseListUserInteractor,
    private val deadlinesSynchronizationInteractor: DeadlinesSynchronizationInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,

    @UserCoursesLoadedBus
    private val userCoursesLoadedPublisher: PublishSubject<UserCoursesLoaded>,
    @EnrollmentCourseUpdates
    private val enrollmentUpdatesObservable: Observable<Course>,
    @UserCoursesUpdateBus
    private val userCoursesUpdateObservable: Observable<Course>,
    @UserCoursesOperationBus
    private val userCourseOperationObservable: Observable<UserCourse>,

    private val progressObservable: Observable<Progress>,

    viewContainer: PresenterViewContainer<CourseListUserView>,
    continueCoursePresenterDelegate: CourseContinuePresenterDelegateImpl
) : PresenterBase<CourseListUserView>(viewContainer), CourseContinuePresenterDelegate by continueCoursePresenterDelegate {
    companion object {
        private const val PAGE_SIZE = 20
    }

    override val delegates: List<PresenterDelegate<in CourseListUserView>> =
        listOf(continueCoursePresenterDelegate)

    private var state: CourseListUserView.State = CourseListUserView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private val paginationDisposable = CompositeDisposable()

    init {
        compositeDisposable += paginationDisposable

        subscribeForEnrollmentUpdates()
        subscribeForContinueCourseUpdates()
        subscribeForUserCourseOperationUpdates()
        subscribeForProgressesUpdates()
    }

    override fun attachView(view: CourseListUserView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchUserCourses(userCourseQuery: UserCourseQuery, forceUpdate: Boolean = false) {
        if (state != CourseListUserView.State.Idle && !forceUpdate) return

        paginationDisposable.clear()

        state = CourseListUserView.State.Loading

        paginationDisposable += courseListUserInteractor
            .getAllUserCourses(userCourseQuery, sourceType = DataSourceType.REMOTE)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    state = CourseListUserView.State.Data(
                        userCourseQuery = userCourseQuery,
                        userCourses = it,
                        courseListViewState = CourseListView.State.Loading
                    )
                    if (userCourseQuery.isMainTab()) {
                        analytic.setCoursesCount(it.size)
                        synchronizeDeadlines()
                    }
                    fetchCourses()
                },
                onError = {
                    if (userCourseQuery.isMainTab()) {
                        userCoursesLoadedPublisher.onNext(UserCoursesLoaded.Empty)
                    }
                    state =
                        if (it is HttpException && it.code() == 401) {
                            CourseListUserView.State.EmptyLogin
                        } else {
                            CourseListUserView.State.NetworkError
                        }
                }
            )
    }

    private fun fetchCourses() {
        val oldState = state as? CourseListUserView.State.Data
            ?: return

        paginationDisposable.clear()

        val ids = oldState
            .userCourses
            .slice(to = PAGE_SIZE)
            .map(UserCourse::course)

        paginationDisposable += Single
            .concat(
                courseListUserInteractor
                    .getCourseListItems(ids, sourceType = DataSourceType.CACHE),
                courseListUserInteractor
                    .getCourseListItems(ids, sourceType = DataSourceType.REMOTE)
            )
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { (items, sourceType) ->
                    state = courseListUserStateMapper.mapToFetchCoursesSuccess(state, items, sourceType == DataSourceType.CACHE)
                    publishUserCoursesLoaded()
                },
                onError = {
                    state = courseListUserStateMapper.mapToFetchCoursesError(state)
                    publishUserCoursesLoaded()
                    view?.showNetworkError()
                }
            )
    }

    fun fetchNextPage() {
        val oldState = state as? CourseListUserView.State.Data
            ?: return

        val oldCourseListState = oldState.courseListViewState as? CourseListView.State.Content
            ?: return

        val ids = courseListUserStateMapper.getNextPageCourseIds(oldState.userCourses, oldCourseListState)
            ?: return

        state = oldState.copy(courseListViewState = courseListStateMapper.mapToLoadMoreState(oldCourseListState))
        paginationDisposable += Single
            .concat(
                courseListUserInteractor
                    .getCourseListItems(ids, sourceType = DataSourceType.CACHE),
                courseListUserInteractor
                    .getCourseListItems(ids, sourceType = DataSourceType.REMOTE)
            )
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { (items, sourceType) ->
                    state = courseListUserStateMapper.mapToFetchCoursesSuccess(state, items, sourceType == DataSourceType.CACHE)
                },
                onError = {
                    state = courseListUserStateMapper.mapToFetchCoursesError(state)
                    view?.showNetworkError()
                }
            )
    }

    /**
     * Continue course
     */
    private fun subscribeForContinueCourseUpdates() {
        compositeDisposable += userCoursesUpdateObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { course ->
                    val (newState, isNeedLoadCourse) =
                        courseListUserStateMapper.mergeWithCourseContinue(state, course.id)
                    state = newState

                    if (isNeedLoadCourse) {
                        fetchPlaceholder(course.id)
                    }
                },
                onError = emptyOnErrorStub
            )
    }

    /**
     * User Course operations
     */
    private fun subscribeForUserCourseOperationUpdates() {
        compositeDisposable += userCourseOperationObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { userCourse ->
                    val (newState, isNeedLoadCourse) =
                        courseListUserStateMapper.mergeWithUserCourse(state, userCourse)
                    state = newState
                    if (isNeedLoadCourse) {
                        fetchPlaceholder(userCourse.course)
                    }
                },
                onError = emptyOnErrorStub
            )
    }

    /**
     * Enrollments
     */
    private fun subscribeForEnrollmentUpdates() {
        compositeDisposable += enrollmentUpdatesObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { course ->
                    if (course.enrollment == 0L) {
                        state = courseListUserStateMapper.mergeWithRemovedCourse(state, course.id)
                        publishUserCoursesLoaded()
                    } else {
                        val (newState, isNeedLoadCourse) =
                            courseListUserStateMapper.mergeWithEnrolledCourse(state, course.id)

                        state = newState
                        if (isNeedLoadCourse) {
                            fetchPlaceholder(course.id)
                        }
                    }
                },
                onError = emptyOnErrorStub
            )
    }

    /**
     * Placeholders
     */
    private fun fetchPlaceholder(courseId: Long) {
        compositeDisposable += courseListUserInteractor
            .getUserCourse(courseId, sourceType = DataSourceType.CACHE)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    state = courseListUserStateMapper.mergeWithPlaceholderSuccess(state, it)
                    publishUserCoursesLoaded() // as we can load top element
                },
                onError = { state = courseListUserStateMapper.mergeWithRemovedCourse(state, courseId) }
            )
    }

    /**
     * Progresses
     */
    private fun subscribeForProgressesUpdates() {
        compositeDisposable += progressObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { progress ->
                    val oldState = (state as? CourseListUserView.State.Data) ?: return@subscribeBy
                    state = oldState.copy(courseListViewState = courseListStateMapper.mergeWithCourseProgress(oldState.courseListViewState, progress))
                },
                onError = emptyOnErrorStub
            )
    }

    private fun publishUserCoursesLoaded() {
        val oldState = (state as? CourseListUserView.State.Data)
            ?.takeIf { it.userCourseQuery.isMainTab() } // only for main tab
            ?: return

        userCoursesLoadedPublisher
            .onNext(courseListUserStateMapper.getLatestCourseToPublish(oldState))
    }

    private fun synchronizeDeadlines() {
        compositeDisposable += deadlinesSynchronizationInteractor
            .syncPersonalDeadlines()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(onError = emptyOnErrorStub)
    }

    private fun UserCourseQuery.isMainTab(): Boolean =
        isArchived == false && isFavorite == null
}