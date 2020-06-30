package org.stepik.android.presentation.course_list

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Course
import org.stepik.android.model.CourseCollection
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

class CourseListCollectionPresenter
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

    viewContainer: PresenterViewContainer<CourseListCollectionView>,
    continueCoursePresenterDelegate: CourseContinuePresenterDelegateImpl
) : PresenterBase<CourseListCollectionView>(viewContainer),
    CourseContinuePresenterDelegate by continueCoursePresenterDelegate,
    CatalogItem {
    override val delegates: List<PresenterDelegate<in CourseListCollectionView>> =
        listOf(continueCoursePresenterDelegate)

    private var state: CourseListCollectionView.State = CourseListCollectionView.State.Idle
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

    override fun attachView(view: CourseListCollectionView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchCourses(courseCollection: CourseCollection, forceUpdate: Boolean = false) {
        if (state != CourseListCollectionView.State.Idle && !forceUpdate) return

        paginationDisposable.clear()

        val viewSource = CourseViewSource.Collection(courseCollection.id)

        state = CourseListCollectionView.State.Data(courseCollection, CourseListView.State.Loading)
        paginationDisposable += Single
            .concat(
                courseListInteractor
                    .getCourseListItems(*courseCollection.courses, sourceType = DataSourceType.CACHE, courseViewSource = viewSource),

                courseListInteractor
                    .getCourseListItems(*courseCollection.courses, sourceType = DataSourceType.REMOTE, courseViewSource = viewSource)
            )
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { items ->
                    val courseListViewState =
                        if (items.isNotEmpty()) {
                            CourseListView.State.Content(courseListDataItems = items, courseListItems = items)
                        } else {
                            CourseListView.State.Empty
                        }

                    state = CourseListCollectionView.State.Data(courseCollection, courseListViewState)
                },
                onError = {
                    val oldCourseListViewState = (state as? CourseListCollectionView.State.Data)
                        ?.courseListViewState

                    when (oldCourseListViewState) {
                        is CourseListView.State.Content -> {
                            state = CourseListCollectionView.State.Data(courseCollection, oldCourseListViewState)
                            view?.showNetworkError()
                        }
                        else ->
                            state = CourseListCollectionView.State.Data(courseCollection, CourseListView.State.NetworkError)
                    }
                }
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
                    val oldState = (state as? CourseListCollectionView.State.Data)
                        ?: return@subscribeBy

                    state = oldState.copy(courseListViewState = courseListStateMapper.mapToEnrollmentUpdateState(oldState.courseListViewState, enrolledCourse))
                    fetchForEnrollmentUpdate(enrolledCourse)
                },
                onError = emptyOnErrorStub
            )
    }

    private fun fetchForEnrollmentUpdate(course: Course) {
        val oldState = (state as? CourseListCollectionView.State.Data)
            ?: return

        compositeDisposable += courseListInteractor
            .getCourseListItems(course.id, courseViewSource = CourseViewSource.Collection(oldState.courseCollection.id), sourceType = DataSourceType.CACHE)
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
                    val oldState = (state as? CourseListCollectionView.State.Data)
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