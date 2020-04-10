package org.stepik.android.presentation.catalog

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.features.stories.presentation.StoriesPresenter
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.catalog.interactor.CatalogInteractor
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.course_list.CourseListPresenter
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
    private val courseListPresenter: CourseListPresenter
) : PresenterBase<CatalogView>() {

    private var state: CatalogView.State = CatalogView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override val nestedDisposables: List<DisposableViewModel>
        get() = (state as? CatalogView.State.Content)
            ?.collections
            ?.filterIsInstance<DisposableViewModel>()
            ?: emptyList()

    init {
        subscribeForFilterUpdates()
    }

    fun fetchCollections(forceUpdate: Boolean = false) {
        if (state != CatalogView.State.Idle && !forceUpdate) return
        compositeDisposable += catalogInteractor
            .fetchCourseCollections()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { courseCollections ->
                    val base = listOf<CatalogItem>(storiesPresenter, tagsPresenter, filtersPresenter)
                    val collections = courseCollections.map {
                        courseListCollectionPresenterProvider.get().apply { setDataToPresenter(it) }
                    }
                    state = CatalogView.State.Content(base + collections)
                },
                onError = {}
            )
    }

    private fun subscribeForFilterUpdates() {
        compositeDisposable += filtersObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = {
                    catalogInteractor.updateFiltersForFeatured(it)
                    fetchCollections(forceUpdate = true)
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