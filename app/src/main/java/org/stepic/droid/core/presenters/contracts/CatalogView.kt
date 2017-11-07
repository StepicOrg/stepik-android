package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.CoursesCarouselInfo

interface CatalogView {

    fun showCarousels(courseItems: List<CoursesCarouselInfo>)

    fun offlineMode()
}
