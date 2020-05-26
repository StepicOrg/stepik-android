package org.stepik.android.presentation.course_list

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.emptyOnErrorStub
import org.stepic.droid.util.mapToLongArray
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
        subscribeForEnrollmentUpdates()
        subscribeForContinueCourseUpdates()
        subscribeForUserCourseOperationUpdates()
    }

    init {
        compositeDisposable += paginationDisposable
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
            .getAllUserCourses(userCourseQuery)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    state = CourseListUserView.State.Data(
                        userCourseQuery = userCourseQuery,
                        userCourses = it,
                        courseListViewState = CourseListView.State.Idle
                    )
                    analytic.setCoursesCount(it.size)
                    fetchCourses()
                },
                onError = {
                    userCoursesLoadedPublisher.onNext(UserCoursesLoaded.Empty)
                    state = if (it is HttpException && it.code() == 401) {
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

        val ids = oldState.userCourses.take(PAGE_SIZE).mapToLongArray(UserCourse::course)

        state = oldState.copy(courseListViewState = CourseListView.State.Loading)

        paginationDisposable += courseListUserInteractor
            .getCourseListItems(*ids)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    state = if (it.isNotEmpty()) {
                        userCoursesLoadedPublisher.onNext(UserCoursesLoaded.FirstCourse(it.first()))
                        oldState.copy(
                            courseListViewState = CourseListView.State.Content(
                                courseListDataItems = PagedList(
                                    list = it,
                                    page = 1,
                                    hasNext = oldState.userCourses.size > PAGE_SIZE,
                                    hasPrev = false
                                ),
                                courseListItems = it
                            )
                        )
                    } else {
                        userCoursesLoadedPublisher.onNext(UserCoursesLoaded.Empty)
                        (state as CourseListUserView.State.Data).copy(
                            courseListViewState = CourseListView.State.Empty
                        )
                    }
                    synchronizeDeadlines()
                },
                onError = {
                    when (val oldCourseListState = (state as? CourseListUserView.State.Data)?.courseListViewState) {
                        is CourseListView.State.Content -> {
                            state = (state as CourseListUserView.State.Data).copy(courseListViewState = oldCourseListState)
                            view?.showNetworkError()
                        }
                        else -> {
                            userCoursesLoadedPublisher.onNext(UserCoursesLoaded.Empty)
                            state = oldState.copy(courseListViewState = CourseListView.State.NetworkError)
                        }
                    }
                }
            )
    }

    fun fetchNextPage() {
        val oldState = state as? CourseListUserView.State.Data
            ?: return

        val oldCourseListState = oldState.courseListViewState as? CourseListView.State.Content
            ?: return

        if (oldCourseListState.courseListItems.last() is CourseListItem.PlaceHolder || !oldCourseListState.courseListDataItems.hasNext) {
            return
        }

        val ids = oldState
            .userCourses
            .drop(oldState.courseListViewState.courseListDataItems.size)
            .take(PAGE_SIZE)
            .mapToLongArray(UserCourse::course)

        state = oldState.copy(courseListViewState = courseListStateMapper.mapToLoadMoreState(oldCourseListState))
        paginationDisposable += courseListUserInteractor
            .getCourseListItems(*ids)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    state = oldState.copy(courseListViewState = courseListStateMapper.mapFromLoadMoreToSuccess(oldCourseListState, it))
                },
                onError = {
                    state = oldState.copy(courseListViewState = courseListStateMapper.mapFromLoadMoreToError(oldCourseListState))
                    view?.showNetworkError()
                }
            )
    }

    private fun subscribeForContinueCourseUpdates() {
        compositeDisposable += userCoursesUpdateObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { continuedCourse ->
                    val oldState = state as? CourseListUserView.State.Data
                        ?: return@subscribeBy

                    state = oldState.copy(
                        courseListViewState = courseListStateMapper.mapToContinueCourseUpdateState(oldState.courseListViewState, continuedCourse).apply {
                            if (this is CourseListView.State.Content) {
                                userCoursesLoadedPublisher.onNext(UserCoursesLoaded.FirstCourse(courseListDataItems.first()))
                            }
                        }
                    )
                },
                onError = emptyOnErrorStub
            )
    }

    private fun subscribeForEnrollmentUpdates() {
        compositeDisposable += enrollmentUpdatesObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { enrollmentCourseUpdate ->
                    if (enrollmentCourseUpdate.enrollment == 0L) {
                        analytic.reportEvent(Analytic.Course.DROP_COURSE_SUCCESSFUL, enrollmentCourseUpdate.id.toString())
                        removeDroppedCourse(enrollmentCourseUpdate.id)
                    } else {
                        fetchEnrolledCourse(enrollmentCourseUpdate.id)
                    }
                },
                onError = emptyOnErrorStub
            )
    }

    private fun subscribeForUserCourseOperationUpdates() {
        compositeDisposable += userCourseOperationObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { userCourse ->
                    val oldState = state as? CourseListUserView.State.Data
                        ?: return@subscribeBy
                    state = courseListStateMapper.mapUserCourseOperationToState(userCourse, oldState)
                    fetchPlaceHolders()
                },
                onError = emptyOnErrorStub
            )
    }

    private fun fetchPlaceHolders() {
        val oldState = (state as? CourseListUserView.State.Data) ?: return

        val oldCourseListState = (oldState.courseListViewState as? CourseListView.State.Content) ?: return

        val indexOf = oldCourseListState.courseListItems.indexOfFirst { it is CourseListItem.PlaceHolder && it.courseId != -1L }

        val courseId = (oldCourseListState.courseListItems[indexOf] as? CourseListItem.PlaceHolder)?.courseId ?: return

        compositeDisposable += courseListUserInteractor
            .getUserCourse(courseId)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { courseListItem ->
                    val oldCourseUserState = state as? CourseListUserView.State.Data
                        ?: return@subscribeBy
                    state = oldCourseUserState.copy(
                        courseListViewState = courseListStateMapper.mapEnrolledCourseListItemState(indexOf, oldState.courseListViewState, courseListItem)
                    )
                },
                onError = emptyOnErrorStub
            )
    }

    private fun removeDroppedCourse(courseId: Long) {
        val oldState = state as? CourseListUserView.State.Data
            ?: return

        val oldCourseListState = oldState.courseListViewState as? CourseListView.State.Content
            ?: return

        val resultState = courseListStateMapper.mapUserCourseRemoveState(oldState, oldCourseListState, courseId)

        val publishUserCourses =
            if (resultState.courseListViewState is CourseListView.State.Content) {
                UserCoursesLoaded.FirstCourse(resultState.courseListViewState.courseListDataItems.first())
            } else {
                UserCoursesLoaded.Empty
            }

        userCoursesLoadedPublisher.onNext(publishUserCourses)
        state = resultState
    }

    private fun fetchEnrolledCourse(courseId: Long) {
        compositeDisposable += courseListUserInteractor
            .getUserCourse(courseId)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { enrolledCourseListItem ->
                    val oldState = state as? CourseListUserView.State.Data
                        ?: return@subscribeBy

                    userCoursesLoadedPublisher.onNext(UserCoursesLoaded.FirstCourse(enrolledCourseListItem))
                    val userCourse = UserCourse(
                        id = 0,
                        user = 0,
                        course = enrolledCourseListItem.id,
                        isFavorite = false,
                        isPinned = false,
                        isArchived = false,
                        lastViewed = null
                    )
                    state = oldState.copy(
                        userCourses = listOf(userCourse) + oldState.userCourses,
                        courseListViewState = courseListStateMapper.mapEnrolledCourseListItemState(0, oldState.courseListViewState, enrolledCourseListItem)
                    )
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