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
import org.stepik.android.domain.course_collection.interactor.CourseCollectionInteractor
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Course
import org.stepik.android.model.CourseCollection
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.mapper.CourseListCollectionStateMapper
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.course_list.UserCoursesOperationBus
import org.stepik.android.view.injection.course_list.WishlistOperationBus
import ru.nobird.android.core.model.cast
import ru.nobird.android.core.model.safeCast
import ru.nobird.android.core.model.slice
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
    private val courseListCollectionStateMapper: CourseListCollectionStateMapper,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    @EnrollmentCourseUpdates
    private val enrollmentUpdatesObservable: Observable<Course>,
    @UserCoursesOperationBus
    private val userCourseOperationObservable: Observable<UserCourse>,
    @WishlistOperationBus
    private val wishlistOperationObservable: Observable<Long>,

    viewContainer: PresenterViewContainer<CourseListCollectionView>,
    continueCoursePresenterDelegate: CourseContinuePresenterDelegateImpl
) : PresenterBase<CourseListCollectionView>(viewContainer),
    CourseContinuePresenterDelegate by continueCoursePresenterDelegate {
    companion object {
        private const val PAGE_SIZE = 20
    }
    override val delegates: List<PresenterDelegate<in CourseListCollectionView>> =
        listOf(continueCoursePresenterDelegate)

    private var state: CourseListCollectionView.State = CourseListCollectionView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private val paginationDisposable = CompositeDisposable()

    init {
        compositeDisposable += paginationDisposable
        subscribeForEnrollmentUpdates()
        subscribeForUserCourseOperationUpdates()
        subscribeForWishlistOperationUpdates()
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
        paginationDisposable += courseCollectionInteractor
            .getCourseCollectionResult(courseCollectionId, viewSource)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { courseCollectionResult ->
                    val newState = courseListCollectionStateMapper.mapCourseCollectionResultToState(courseCollectionResult)
                    val isNeedLoadNextPage = courseListCollectionStateMapper.isNeedLoadNextPage(newState)
                    state = newState

                    if (isNeedLoadNextPage) {
                        fetchNextPage()
                    }
                },
                onError = {
                    when (val oldState = state.safeCast<CourseListCollectionView.State.Data>()?.courseListViewState) {
                        is CourseListView.State.Content -> {
                            state = state.cast<CourseListCollectionView.State.Data>()
                                .copy(
                                    courseListViewState = oldState.copy(oldState.courseListDataItems, oldState.courseListItems),
                                    sourceType = null
                                )
                            view?.showNetworkError()
                        }
                        else ->
                            state = CourseListCollectionView.State.NetworkError
                    }
                }
            )
    }

    fun fetchNextPage() {
        val oldState = state as? CourseListCollectionView.State.Data
            ?: return

        val oldCourseListState = oldState.courseListViewState as? CourseListView.State.Content
            ?: return

        val lastItem = oldCourseListState.courseListItems.last()
        if (lastItem is CourseListItem.SimilarAuthors || lastItem is CourseListItem.SimilarCourses) {
            return
        }

        val ids = getNextPageCourseIds(oldState.courseCollection, oldCourseListState)
            ?.takeIf { it.isNotEmpty() }
            ?: return

        state = oldState.copy(courseListViewState = courseListStateMapper.mapToLoadMoreState(oldCourseListState))
        if (oldState.sourceType != DataSourceType.CACHE) {
            paginationDisposable += courseCollectionInteractor
                .getCourseListItems(ids, courseViewSource = CourseViewSource.Collection(oldState.courseCollection.id))
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

    private fun getNextPageCourseIds(courseCollection: CourseCollection, courseListViewState: CourseListView.State.Content): List<Long>? {
        if ((courseListViewState.courseListItems.last() as? CourseListItem.PlaceHolder)?.courseId == -1L) {
            return null
        }

        val offset = courseListViewState.courseListItems.size
        return courseCollection
            .courses
            .slice(offset, offset + PAGE_SIZE)
            .map { it }
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

        compositeDisposable += courseCollectionInteractor
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

    /**
     * Wishlist updates
     */
    private fun subscribeForWishlistOperationUpdates() {
        compositeDisposable += wishlistOperationObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = {
                    val oldState = (state as? CourseListCollectionView.State.Data)
                        ?: return@subscribeBy

                    state = oldState.copy(courseListViewState = courseListStateMapper.mapToWishlistUpdate(oldState.courseListViewState, it))
                },
                onError = emptyOnErrorStub
            )
    }

    public override fun onCleared() {
        super.onCleared()
    }
}