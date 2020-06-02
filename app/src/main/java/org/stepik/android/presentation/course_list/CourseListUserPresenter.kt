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
import org.stepic.droid.util.emptyOnErrorStub
import org.stepic.droid.util.mapToLongArray
import org.stepic.droid.util.takeLazy
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.interactor.CourseListUserInteractor
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.course_list.model.UserCoursesLoaded
import org.stepik.android.domain.personal_deadlines.interactor.DeadlinesSynchronizationInteractor
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.presentation.course_list.mapper.CourseListUserStateMapper
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.course_list.UserCoursesLoadedBus
import org.stepik.android.view.injection.course_list.UserCoursesOperationBus
import org.stepik.android.view.injection.course_list.UserCoursesUpdateBus
import retrofit2.HttpException
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
                        courseListViewState = CourseListView.State.Idle
                    )
                    analytic.setCoursesCount(it.size) // todo fix analytics
                    fetchCourses()
                    synchronizeDeadlines()
                },
                onError = {
                    userCoursesLoadedPublisher.onNext(UserCoursesLoaded.Empty)
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

        paginationDisposable.clear() // todo ??

        val ids = oldState
            .userCourses
            .takeLazy(PAGE_SIZE)
            .mapToLongArray(UserCourse::course)

        state = oldState.copy(courseListViewState = CourseListView.State.Loading)

        paginationDisposable += Single
            .concat(
                courseListUserInteractor
                    .getCourseListItems(*ids, sourceType = DataSourceType.CACHE),
                courseListUserInteractor
                    .getCourseListItems(*ids, sourceType = DataSourceType.REMOTE)
            )
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { (items, sourceType) ->
                    val newState = courseListUserStateMapper.mapToFetchCoursesSuccess(state, items, sourceType == DataSourceType.CACHE)
                    if (newState is CourseListUserView.State.Data) {
                        val userCourseLoaded = (newState.courseListViewState as? CourseListView.State.Content)
                            ?.courseListDataItems
                            ?.firstOrNull()
                            ?.let(UserCoursesLoaded::FirstCourse)
                            ?: UserCoursesLoaded.Empty

                        userCoursesLoadedPublisher.onNext(userCourseLoaded)
                    }
                    state = newState
                },
                onError = {
                    val newState = courseListUserStateMapper.mapToFetchCoursesError(state)
                    when ((newState as? CourseListUserView.State.Data)?.courseListViewState) {
                        is CourseListView.State.Content ->
                            view?.showNetworkError()

                        else ->
                            userCoursesLoadedPublisher.onNext(UserCoursesLoaded.Empty)
                    }
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
                    .getCourseListItems(*ids, sourceType = DataSourceType.CACHE),
                courseListUserInteractor
                    .getCourseListItems(*ids, sourceType = DataSourceType.REMOTE)
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
                    val oldState = state as? CourseListUserView.State.Data
                        ?: return@subscribeBy
                    state = courseListStateMapper.mapUserCourseOperationToState(userCourse, oldState)
//                    fetchPlaceHolders()
                },
                onError = emptyOnErrorStub
            )
    }

    private fun fetchPlaceholder(courseId: Long) {
        compositeDisposable += courseListUserInteractor
            .getUserCourse(courseId)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { state = courseListUserStateMapper.mergeWithPlaceholderSuccess(state, it) },
                onError = { state = courseListUserStateMapper.mergeWithDroppedCourse(state, courseId) }
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
                        val newState = courseListUserStateMapper.mergeWithDroppedCourse(state, course.id)
                        state = newState

                        if (newState is CourseListUserView.State.Data) {
                            val publishUserCourses =
                                if (newState.courseListViewState is CourseListView.State.Content) {
                                    UserCoursesLoaded.FirstCourse(newState.courseListViewState.courseListDataItems.first())
                                } else {
                                    UserCoursesLoaded.Empty
                                }
                            userCoursesLoadedPublisher.onNext(publishUserCourses)
                        }
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

    private fun synchronizeDeadlines() {
        compositeDisposable += deadlinesSynchronizationInteractor
            .syncPersonalDeadlines()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(onError = emptyOnErrorStub)
    }
}