package org.stepik.android.presentation.course_list

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.course.analytic.CourseViewSource
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

        val oldCourseListViewState = (state as? CourseListCollectionView.State.Data)
            ?.courseListViewState

        state = CourseListCollectionView.State.Data(courseCollection, CourseListView.State.Loading)
        paginationDisposable += courseListInteractor
            .getCourseListItems(*courseCollection.courses, courseViewSource = CourseViewSource.Collection(courseCollection.id))
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { items ->
                    val courseListViewState =
                        if (items.isNotEmpty()) {
                            CourseListView.State.Content(
                                courseListDataItems = items,
                                courseListItems = items
                            )
                        } else {
                            CourseListView.State.Empty
                        }

                    state = CourseListCollectionView.State.Data(courseCollection, courseListViewState)
                },
                onError = {
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

    private fun subscribeForEnrollmentUpdates() {
        compositeDisposable += enrollmentUpdatesObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { enrolledCourse ->
                    val oldState = (state as? CourseListCollectionView.State.Data)
                        ?: return@subscribeBy

                    state = oldState.copy(courseListViewState = courseListStateMapper.mapToEnrollmentUpdateState(oldState.courseListViewState, enrolledCourse))
                },
                onError = emptyOnErrorStub
            )
    }

    public override fun onCleared() {
        super.onCleared()
    }
}