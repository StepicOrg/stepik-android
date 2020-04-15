package org.stepik.android.presentation.course_list

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.model.Course
import org.stepik.android.presentation.catalog.model.CatalogItem
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
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

        paginationDisposable += courseListInteractor
            .getCourseListItems(courseListQuery)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    state = if (it.isNotEmpty()) {
                        (state as CourseListQueryView.State.Data).copy(
                            courseListViewState = CourseListView.State.Content(
                                courseListDataItems = it,
                                courseListItems = it
                            )
                        )
                    } else {
                        (state as CourseListQueryView.State.Data).copy(
                            courseListViewState = CourseListView.State.Empty
                        )
                    }
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

    fun fetchNextPage() {
        val oldState = (state as? CourseListQueryView.State.Data)
            ?: return

        val oldCourseListState = oldState.courseListViewState as? CourseListView.State.Content
            ?: return

        if (oldCourseListState.courseListItems.last() is CourseListItem.PlaceHolder ||
            !oldCourseListState.courseListDataItems.hasNext) {
            return
        }

        val nextPage = oldCourseListState.courseListDataItems.page + 1

        val courseListQuery = oldState.courseListQuery.copy(page = nextPage)

        state = oldState.copy(courseListViewState = courseListStateMapper.mapToLoadMoreState(oldCourseListState))
        paginationDisposable += courseListInteractor
            .getCourseListItems(courseListQuery)
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

    public override fun onCleared() {
        super.onCleared()
    }

    private fun subscribeForEnrollmentUpdates() {
        compositeDisposable += enrollmentUpdatesObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { enrolledCourse ->
                    val oldState = (state as? CourseListQueryView.State.Data)
                        ?: return@subscribeBy

                    val oldCourseListState = oldState.courseListViewState as? CourseListView.State.Content
                        ?: return@subscribeBy

                    state = oldState.copy(courseListViewState = courseListStateMapper.mapToEnrollmentUpdateState(oldCourseListState, enrolledCourse))
                },
                onError = emptyOnErrorStub
            )
    }
}