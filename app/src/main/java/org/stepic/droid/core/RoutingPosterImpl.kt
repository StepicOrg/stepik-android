package org.stepic.droid.core

import org.stepic.droid.model.Section
import javax.inject.Inject

class RoutingPosterImpl
@Inject constructor(
        private val routingListenerContainer: RoutingListenerContainer)
    : RoutingPoster {

    override fun onSectionChanged(oldSection: Section, newSection: Section) {
        routingListenerContainer.iterator().forEach {
            it.onSectionChanged(oldSection, newSection)
        }
    }
}
