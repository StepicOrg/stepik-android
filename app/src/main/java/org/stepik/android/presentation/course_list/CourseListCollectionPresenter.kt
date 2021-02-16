package org.stepik.android.presentation.course_list

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.catalog.model.CatalogAuthor
import org.stepik.android.domain.catalog.model.CatalogCourseList
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course.model.SourceTypeComposition
import org.stepik.android.domain.course_collection.interactor.CourseCollectionInteractor
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
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
                            Single.just(CourseListCollectionView.State.Data(
                                collection,
                                CourseListView.State.Empty,
                                sourceType = sourceType.generalSourceType
                            ))
                        } else {
                            if (collection.similarCourseLists.isNotEmpty()) {
                                zip(
                                    courseListInteractor
                                        .getCourseListItems(collection.courses, sourceTypeComposition = sourceType, courseViewSource = viewSource),
                                    courseCollectionInteractor
                                        .getSimilarCourseLists(collection.similarCourseLists, dataSource = sourceType.generalSourceType),
                                    courseCollectionInteractor
                                        .getSimilarAuthorLists(collection.similarAuthors, dataSource = sourceType.generalSourceType)
                                ) { items, similarCourses, authors ->
                                    CourseListCollectionView.State.Data(
                                        collection,
                                        CourseListView.State.Content(
                                            courseListDataItems = items,
                                            courseListItems = items + formHorizontalLists(authors, similarCourses)
                                        ),
                                        sourceType.generalSourceType
                                    )
                                }
                            } else {
                                val ids = collection.courses.take(PAGE_SIZE)
                                courseListInteractor
                                    .getCourseListItems(ids, sourceTypeComposition = sourceType, courseViewSource = viewSource)
                                    .map { items ->
                                        CourseListCollectionView.State.Data(
                                            collection,
                                            CourseListView.State.Content(courseListDataItems = items, courseListItems = items),
                                            sourceType = sourceType.generalSourceType
                                        )
                                    }
                            }
                        }
                    }
            }
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = {
                    val isNeedLoadNextPage = courseListCollectionStateMapper.isNeedLoadNextPage(it)
                    state = it

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
            paginationDisposable += courseListInteractor
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

    private fun formHorizontalLists(authors: List<CatalogAuthor>, similarCourses: List<CatalogCourseList>): List<CourseListItem> {
        val list = mutableListOf<CourseListItem>()
        if (authors.isNotEmpty()) {
            list += CourseListItem.SimilarAuthors(authors)
        }
        if (similarCourses.isNotEmpty()) {
            list += CourseListItem.SimilarCourses(similarCourses)
        }
        return list
    }

    public override fun onCleared() {
        super.onCleared()
    }
}