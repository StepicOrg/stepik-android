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
import org.stepik.android.model.Course
import org.stepik.android.model.CourseCollection
import org.stepik.android.presentation.catalog.model.CatalogItem
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
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
    }

    override fun attachView(view: CourseListCollectionView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchCourses(courseCollection: CourseCollection, forceUpdate: Boolean = false) {
        if (state != CourseListCollectionView.State.Idle && !forceUpdate) return

        paginationDisposable.clear()

        val oldState = state

        state = CourseListCollectionView.State.Data(
            courseCollection = courseCollection,
            courseListViewState = CourseListView.State.Loading
        )

        paginationDisposable += courseListInteractor
            .getCourseListItems(*courseCollection.courses)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    state = if (it.isNotEmpty()) {
                        (state as CourseListCollectionView.State.Data)
                            .copy(courseListViewState = CourseListView.State.Content(
                                courseListDataItems = it,
                                courseListItems = it
                            )
                        )
                    } else {
                        (state as CourseListCollectionView.State.Data)
                            .copy(courseListViewState = CourseListView.State.Empty)
                    }
                },
                onError = {
                    when ((oldState as CourseListCollectionView.State.Data).courseListViewState) {
                        is CourseListView.State.Content -> {
                            state = oldState
                            view?.showNetworkError()
                        }
                        else ->
                            state = oldState.copy(courseListViewState = CourseListView.State.NetworkError)
                    }
                }
            )
    }

    private fun subscribeForEnrollmentUpdates() {
        compositeDisposable += enrollmentUpdatesObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { enrolledCourse ->
                    val oldState = (state as? CourseListCollectionView.State.Data)
                        ?: return@subscribeBy

                    val oldCourseListState = oldState.courseListViewState as? CourseListView.State.Content
                        ?: return@subscribeBy

                    state = oldState.copy(courseListViewState = courseListStateMapper.mapToEnrollmentUpdateState(oldCourseListState, enrolledCourse))
                },
                onError = emptyOnErrorStub
            )
    }

    public override fun onCleared() {
        super.onCleared()
    }
}