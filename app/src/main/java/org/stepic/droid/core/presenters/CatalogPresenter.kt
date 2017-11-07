package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import org.stepic.droid.core.presenters.contracts.CatalogView
import org.stepic.droid.di.catalog.CatalogScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.mappers.Mapper
import org.stepic.droid.model.CourseListItem
import org.stepic.droid.model.CoursesCarouselInfo
import org.stepic.droid.web.Api
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
        private val mapper: Mapper<CourseListItem, CoursesCarouselInfo>
) : PresenterBase<CatalogView>() {

    fun onCatalogOpened() {
        api
                .courseLists
                .map {
                    mapper.map(it.courseLists)
                }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    view?.showCarousels(it)
                }, {
                    view?.offlineMode()
                })

    }
}
