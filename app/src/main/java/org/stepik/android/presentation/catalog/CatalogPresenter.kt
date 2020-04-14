package org.stepik.android.presentation.catalog

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.features.stories.presentation.StoriesPresenter
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.catalog.interactor.CatalogInteractor
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.course_list.CourseListQueryPresenter
import org.stepik.android.view.injection.catalog.FiltersBus
import ru.nobird.android.presentation.base.DisposableViewModel
import ru.nobird.android.presentation.base.PresenterBase
import java.util.EnumSet
import javax.inject.Inject
import javax.inject.Provider

class CatalogPresenter
@Inject
constructor(
    @FiltersBus
    private val filtersObservable: Observable<EnumSet<StepikFilter>>,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,

    private val catalogInteractor: CatalogInteractor,

    private val storiesPresenter: StoriesPresenter,
    private val tagsPresenter: TagsPresenter,
    private val filtersPresenter: FiltersPresenter,

    private val courseListCollectionPresenterProvider: Provider<CourseListCollectionPresenter>,
    private val courseListQueryPresenter: CourseListQueryPresenter,

    private val sharedPreferenceHelper: SharedPreferenceHelper
) : PresenterBase<CatalogView>() {

    private var state: CatalogView.State =
        CatalogView.State(
            headers = listOf(storiesPresenter, tagsPresenter, filtersPresenter),
            collectionsState = CatalogView.CollectionsState.Idle,
            footers = listOf(courseListQueryPresenter)
        )
        set(value) {
            field = value
            view?.setState(value)
        }

    override val nestedDisposables: List<DisposableViewModel>
        get() = (state.headers + (state.collectionsState as? CatalogView.CollectionsState.Content)?.collections.orEmpty() + state.footers)
            .filterIsInstance<DisposableViewModel>()

    private val collectionsDisposable = CompositeDisposable()

    init {
        compositeDisposable += collectionsDisposable
        subscribeForFilterUpdates()
        fetchPopularCourses()
    }

    override fun attachView(view: CatalogView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchCollections(forceUpdate: Boolean = false) {
        if (state.collectionsState != CatalogView.CollectionsState.Idle && !forceUpdate) return

        state = state.copy(collectionsState = CatalogView.CollectionsState.Loading)

        if (forceUpdate) {
            storiesPresenter.fetchStories(forceUpdate = forceUpdate)
            tagsPresenter.fetchFeaturedTags(forceUpdate = forceUpdate)
        }

        collectionsDisposable += catalogInteractor
            .fetchCourseCollections()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { courseCollections ->
                    val collections = courseCollections.map {
                        courseListCollectionPresenterProvider.get().apply { fetchCourses(it) }
                    }

                    (state.collectionsState as? CatalogView.CollectionsState.Content)
                        ?.collections
                        ?.forEach {
                            it.view?.let(it::detachView)
                            it.onCleared()
                        }

                    state = state.copy(collectionsState = CatalogView.CollectionsState.Content(collections = collections))
                },
                onError = {
                    state = state.copy(collectionsState = CatalogView.CollectionsState.Error)
                }
            )
    }

    private fun fetchPopularCourses() {
        courseListQueryPresenter.onCleared()
        courseListQueryPresenter.fetchCourses(
            courseListQuery = CourseListQuery(
                page = 1,
                order = CourseListQuery.Order.ACTIVITY_DESC,
                language = sharedPreferenceHelper.languageForFeatured,
                isExcludeEnded = true,
                isPublic = true
            )
        )
    }

    private fun subscribeForFilterUpdates() {
        compositeDisposable += filtersObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = {
                    collectionsDisposable.clear()
                    fetchCollections(forceUpdate = true)
                    fetchPopularCourses()
                },
                onError = emptyOnErrorStub
            )
    }

    override fun detachView(view: CatalogView) {
        nestedDisposables
            .filterIsInstance<PresenterBase<Any>>()
            .forEach { it.view?.let(it::detachView) }
        super.detachView(view)
    }
}