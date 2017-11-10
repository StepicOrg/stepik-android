package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.CoursesCarouselInfo

interface CatalogView {

    fun showCollections(courseItems: List<CoursesCarouselInfo>)

    fun offlineMode()
}
