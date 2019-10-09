package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.stepic.droid.core.presenters.contracts.CatalogView
import org.stepic.droid.di.catalog.CatalogScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.mappers.Mapper
import org.stepik.android.model.CourseCollection
import org.stepic.droid.model.CoursesCarouselInfo
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.web.Api
import java.util.*
import javax.inject.Inject

@CatalogScope
class CatalogPresenter
@Inject
constructor(
    private val api: Api,
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
            api.getCourseCollections(lang)
                .map {
                    it.courseCollections.sortedBy {
                        it.position
                    }
                }
                .map {
                    mapper.map(it)
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
