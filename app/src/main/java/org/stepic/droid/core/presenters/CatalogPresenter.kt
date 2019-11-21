package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.stepic.droid.core.presenters.contracts.CatalogView
import org.stepic.droid.di.catalog.CatalogScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.mappers.Mapper
import org.stepic.droid.model.CoursesCarouselInfo
import org.stepic.droid.model.StepikFilter
import org.stepik.android.domain.course_collection.repository.CourseCollectionRepository
import org.stepik.android.model.CourseCollection
import java.util.EnumSet
import javax.inject.Inject

@CatalogScope
class CatalogPresenter
@Inject
constructor(
    private val courseCollectionRepository: CourseCollectionRepository,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val mapper: Mapper<CourseCollection, CoursesCarouselInfo>
) : PresenterBase<CatalogView>() {

    private val disposableContainer = CompositeDisposable()

    fun onNeedLoadCatalog(filters: EnumSet<StepikFilter>) {
        check(filters.size <= 1) { "Filters are corrupted" }

        val lang = filters.first().language
        disposableContainer +=
            courseCollectionRepository.getCourseCollection(lang)
                .map { collections ->
                    collections
                        .sortedBy(CourseCollection::position)
                        .let(mapper::map)
                }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    view?.showCollections(it)
                }, {
                    view?.offlineMode()
                })
    }

    override fun detachView(view: CatalogView) {
        super.detachView(view)
        disposableContainer.clear()
    }
}
