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
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course.model.SourceTypeComposition
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.domain.wishlist.interactor.CourseListWishInteractor
import org.stepik.android.domain.wishlist.model.WishlistWrapper
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.presentation.course_list.mapper.CourseListWishStateMapper
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.course_list.UserCoursesOperationBus
import ru.nobird.android.core.model.cast
import ru.nobird.android.core.model.safeCast
import ru.nobird.android.core.model.slice
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class CourseListWishPresenter
@Inject
constructor(
    private val courseListWishInteractor: CourseListWishInteractor,
    private val courseListStateMapper: CourseListStateMapper,
    private val courseListWishStateMapper: CourseListWishStateMapper,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    @EnrollmentCourseUpdates
    private val enrollmentUpdatesObservable: Observable<Course>,
    @UserCoursesOperationBus
    private val userCourseOperationObservable: Observable<UserCourse>,

    viewContainer: PresenterViewContainer<CourseListWishView>,
    continueCoursePresenterDelegate: CourseContinuePresenterDelegateImpl
) : PresenterBase<CourseListWishView>(viewContainer),
    CourseContinuePresenterDelegate by continueCoursePresenterDelegate {
        companion object {
            private const val PAGE_SIZE = 20
        }

    override val delegates: List<PresenterDelegate<in CourseListWishView>> =
        listOf(continueCoursePresenterDelegate)

    private var state: CourseListWishView.State = CourseListWishView.State.Idle
        set(value) {
            field = value
            view?.setState(state)
        }

    private val paginationDisposable = CompositeDisposable()

    init {
        compositeDisposable += paginationDisposable
        subscribeForEnrollmentUpdates()
        subscribeForUserCourseOperationUpdates()
    }

    override fun attachView(view: CourseListWishView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchCourses(forceUpdate: Boolean = false) {
        if (state != CourseListWishView.State.Idle && !forceUpdate) return

        paginationDisposable.clear()

        paginationDisposable += Flowable
            .fromArray(SourceTypeComposition.CACHE, SourceTypeComposition.REMOTE)
            .concatMapSingle { sourceType ->
                courseListWishInteractor
                    .getWishlistStorageRecord(sourceType.generalSourceType)
                    .flatMap { wishList ->
                        if (wishList.data.courses == null || wishList.data.courses.isEmpty()) {
                            Single.just(CourseListView.State.Empty to sourceType.generalSourceType)
                        } else {
                            val ids = wishList.data.courses.take(PAGE_SIZE)
                            courseListWishInteractor
                                .getCourseListItems(ids, sourceTypeComposition = sourceType, courseViewSource = CourseViewSource.Wishlist)
                                .map { items ->
                                    CourseListView.State.Content(courseListDataItems = items, courseListItems = items) to sourceType.generalSourceType
                                }
                        }.map { (courseListViewState, sourceType) ->
                            CourseListWishView.State.Data(wishList, courseListViewState, sourceType)
                        }
                    }
            }
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = {
                    val isNeedLoadNextPage = courseListWishStateMapper.isNeedLoadNextPage(it)
                    state = it

                    if (isNeedLoadNextPage) {
                        fetchNextPage()
                    }
                },
                onError = {
                    state =
                        when (val oldState = state.safeCast<CourseListWishView.State.Data>()?.courseListViewState) {
                        is CourseListView.State.Content -> {
                            view?.showNetworkError()
                            state.cast<CourseListWishView.State.Data>()
                                .copy(
                                    courseListViewState = oldState.copy(oldState.courseListDataItems, oldState.courseListItems),
                                    sourceType = null
                                )
                        }
                        else ->
                            CourseListWishView.State.NetworkError
                    }
                }
            )
    }

    fun fetchNextPage() {
        val oldState = state as? CourseListWishView.State.Data
            ?: return

        val oldCourseListState = oldState.courseListViewState as? CourseListView.State.Content
            ?: return

        val ids = getNextPageCourseIds(oldState.wishlistRecord, oldCourseListState)
            ?.takeIf { it.isNotEmpty() }
            ?: return

        state = oldState.copy(courseListViewState = courseListStateMapper.mapToLoadMoreState(oldCourseListState))
        if (oldState.sourceType != DataSourceType.CACHE) {
            paginationDisposable += courseListWishInteractor
                .getCourseListItems(ids, courseViewSource = CourseViewSource.Wishlist)
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

    private fun getNextPageCourseIds(wishlistRecord: StorageRecord<WishlistWrapper>, courseListViewState: CourseListView.State.Content): List<Long>? {
        if ((courseListViewState.courseListItems.last() as? CourseListItem.PlaceHolder)?.courseId == -1L) {
            return null
        }

        val wishlistCourses = wishlistRecord
            .data
            .courses
            ?: return null

        val offset = courseListViewState.courseListItems.size

        return wishlistCourses.slice(offset, offset + PAGE_SIZE)
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
                    val oldState = (state as? CourseListWishView.State.Data)
                        ?: return@subscribeBy

                    state = oldState.copy(courseListViewState = courseListStateMapper.mapToEnrollmentUpdateState(oldState.courseListViewState, enrolledCourse))
                    fetchForEnrollmentUpdate(enrolledCourse)
                },
                onError = emptyOnErrorStub
            )
    }

    private fun fetchForEnrollmentUpdate(course: Course) {
        val oldState = (state as? CourseListWishView.State.Data)
            ?: return

        compositeDisposable += courseListWishInteractor
            .getCourseListItems(listOf(course.id), courseViewSource = CourseViewSource.Wishlist, sourceTypeComposition = SourceTypeComposition.CACHE)
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
                    val oldState = (state as? CourseListWishView.State.Data)
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