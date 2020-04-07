package org.stepik.android.presentation.catalog

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.features.stories.presentation.StoriesPresenter
import org.stepik.android.domain.catalog.interactor.CatalogInteractor
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import ru.nobird.android.presentation.base.PresenterBase
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class CatalogPresenter
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val catalogInteractor: CatalogInteractor,
    private val storiesPresenter: StoriesPresenter,
    private val tagsPresenter: TagsPresenter,
    private val filtersPresenter: FiltersPresenter,
    private val courseListCollectionPresenterProvider: Provider<CourseListCollectionPresenter>
) : PresenterBase<CatalogView>() {

    private var state: CatalogView.State = CatalogView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }
    fun fetchCollections() {
        compositeDisposable += catalogInteractor
            .fetchCourseCollections()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { courseCollections ->
                    val base = listOf<CatalogItem>(storiesPresenter, tagsPresenter, filtersPresenter)
                    val collections = courseCollections.map {
                        Timber.d("Collection: $it")
                        courseListCollectionPresenterProvider.get().apply { fetchCourses(*it.courses) }
                    }
                    state = CatalogView.State.Content(base + collections)
                },
                onError = {}
            )
    }
}