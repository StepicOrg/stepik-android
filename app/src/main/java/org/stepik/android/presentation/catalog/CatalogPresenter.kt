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
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepik.android.domain.catalog.interactor.CatalogInteractor
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import org.stepik.android.presentation.catalog.model.CatalogItem
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.course_list.CourseListQueryPresenter
import org.stepik.android.presentation.filter.FiltersPresenter
import org.stepik.android.presentation.stories.StoriesFeature
import org.stepik.android.presentation.stories.StoriesViewModel
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

    private val storiesViewModel: StoriesViewModel,
    private val filtersPresenter: FiltersPresenter,

    private val courseListCollectionPresenterProvider: Provider<CourseListCollectionPresenter>,
    private val courseListQueryPresenter: CourseListQueryPresenter,

    private val sharedPreferenceHelper: SharedPreferenceHelper
) : PresenterBase<CatalogView>() {

    private var state: CatalogView.State =
        CatalogView.State(
            headers = getHeaders(),
            collectionsState = CatalogView.CollectionsState.Idle,
            footers = listOf()
//            footers = listOf(courseListQueryPresenter)
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
        storiesViewModel.onNewMessage(StoriesFeature.Message.InitMessage())
//        subscribeForFilterUpdates()
//        fetchPopularCourses()
    }

    override fun attachView(view: CatalogView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchCollections(forceUpdate: Boolean = false) {
        if (state.collectionsState != CatalogView.CollectionsState.Idle && !forceUpdate) return

        state = state.copy(collectionsState = CatalogView.CollectionsState.Loading)

        if (forceUpdate) {
//            storiesPresenter.fetchStories(forceUpdate = forceUpdate)
//            tagsPresenter.fetchFeaturedTags(forceUpdate = forceUpdate)
            fetchPopularCourses(forceUpdate = forceUpdate)
        }

        collectionsDisposable += catalogInteractor
            .fetchCourseCollections()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { courseCollections ->
                    val collections = courseCollections.map {
                        courseListCollectionPresenterProvider.get().apply { fetchCourses(it.id) }
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

    private fun fetchPopularCourses(forceUpdate: Boolean = false) {
        courseListQueryPresenter.fetchCourses(
            courseListQuery = CourseListQuery(
                page = 1,
                order = CourseListQuery.Order.ACTIVITY_DESC,
                isCataloged = true,
                filterQuery = CourseListFilterQuery(language = sharedPreferenceHelper.languageForFeatured)
            ),
            forceUpdate = forceUpdate
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
                },
                onError = emptyOnErrorStub
            )
    }

    private fun getHeaders(): List<CatalogItem> =
        listOf(storiesViewModel)
//        if (sharedPreferenceHelper.isNeedShowLangWidget) {
//            listOf(storiesPresenter, tagsPresenter, filtersPresenter)
//        } else {
//            listOf(storiesPresenter, tagsPresenter)
//        }

    override fun detachView(view: CatalogView) {
        nestedDisposables
            .filterIsInstance<PresenterBase<Any>>()
            .forEach { it.view?.let(it::detachView) }
        super.detachView(view)
    }
}