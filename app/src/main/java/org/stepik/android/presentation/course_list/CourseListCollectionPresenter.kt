package org.stepik.android.presentation.course_list

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course.model.SourceTypeComposition
import org.stepik.android.domain.course_collection.interactor.CourseCollectionInteractor
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Course
import org.stepik.android.presentation.catalog.model.OldCatalogItem
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.course_list.UserCoursesOperationBus
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class CourseListCollectionPresenter
@Inject
constructor(
    private val courseCollectionInteractor: CourseCollectionInteractor,
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
    OldCatalogItem {
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

    fun fetchCourses(courseCollectionId: Long, forceUpdate: Boolean = false) {
        if (state != CourseListCollectionView.State.Idle && !forceUpdate) return

        paginationDisposable.clear()

        val viewSource = CourseViewSource.Collection(courseCollectionId)

        state = CourseListCollectionView.State.Loading
        paginationDisposable += Flowable
            .fromArray(SourceTypeComposition.CACHE, SourceTypeComposition.REMOTE)
            .concatMapSingle { sourceType ->
                courseCollectionInteractor
                    .getCourseCollection(courseCollectionId, sourceType.generalSourceType)
                    .flatMap { collection ->
                        if (collection.courses.isEmpty()) {
                            Single.just(CourseListView.State.Empty)
                        } else {
                            courseListInteractor
                                .getCourseListItems(collection.courses, sourceTypeComposition = sourceType, courseViewSource = viewSource)
                                .map { items ->
                                    CourseListView.State.Content(courseListDataItems = items, courseListItems = items)
                                }
                        }.map { courseListViewState ->
                            CourseListCollectionView.State.Data(collection, courseListViewState)
                        }
                    }
            }
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { state = it },
                onError = {
                    val oldState = state
                    if (oldState is CourseListCollectionView.State.Data && oldState.courseListViewState is CourseListView.State.Content) {
                        view?.showNetworkError()
                    } else {
                        state = CourseListCollectionView.State.NetworkError
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
            .getCourseListItems(listOf(course.id), courseViewSource = CourseViewSource.Collection(oldState.courseCollection.id), sourceTypeComposition = SourceTypeComposition.CACHE)
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