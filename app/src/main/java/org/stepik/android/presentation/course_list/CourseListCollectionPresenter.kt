package org.stepik.android.presentation.course_list

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.model.CourseCollection
import org.stepik.android.presentation.catalog.CatalogItem
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class CourseListCollectionPresenter
@Inject
constructor(
    private val courseListInteractor: CourseListInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,

    viewContainer: PresenterViewContainer<CourseListView>,
    continueCoursePresenterDelegate: CourseContinuePresenterDelegateImpl
) : PresenterBase<CourseListView>(viewContainer),
    CourseContinuePresenterDelegate by continueCoursePresenterDelegate,
    CatalogItem {
    override val delegates: List<PresenterDelegate<in CourseListView>> =
        listOf(continueCoursePresenterDelegate)

    private var state: CourseListView.State = CourseListView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    var courseCollection: CourseCollection? = null
        private set

    private val paginationDisposable = CompositeDisposable()

    init {
        compositeDisposable += paginationDisposable
    }

    override fun attachView(view: CourseListView) {
        super.attachView(view)
        view.setState(state)
    }

    fun setDataToPresenter(courseCollection: CourseCollection) {
        this.courseCollection = courseCollection
        fetchCourses(forceUpdate = false)
    }

    fun fetchCourses(forceUpdate: Boolean = false) {
        if (state != CourseListView.State.Idle && !forceUpdate) return

        val collection = courseCollection ?: return

        paginationDisposable.clear()

        val oldState = state

        state = CourseListView.State.Loading(
            collectionData = CourseListView.State.CollectionData(
                title = collection.title,
                description = collection.description
            )
        )

        paginationDisposable += courseListInteractor
            .getCourseListItems(*collection.courses)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    state = if (it.isNotEmpty()) {
                        CourseListView.State.Content(
                            collectionData = CourseListView.State.CollectionData(
                                collection.title,
                                collection.description
                            ),
                            courseListDataItems = it,
                            courseListItems = it
                        )
                    } else {
                        CourseListView.State.Empty
                    }
                },
                onError = {
                    when (oldState) {
                        is CourseListView.State.Content -> {
                            state = oldState
                            view?.showNetworkError()
                        }
                        else ->
                            state = CourseListView.State.NetworkError
                    }
                }
            )
    }
}