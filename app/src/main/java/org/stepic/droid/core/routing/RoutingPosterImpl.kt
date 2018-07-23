package org.stepic.droid.core.routing

import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.core.routing.contract.RoutingListener
import org.stepic.droid.core.routing.contract.RoutingPoster
import org.stepik.android.model.Section
import javax.inject.Inject

class RoutingPosterImpl
@Inject constructor(
        private val routingListenerContainer: ListenerContainer<RoutingListener>)
    : RoutingPoster {

    override fun sectionChanged(oldSection: Section, newSection: Section) {
        routingListenerContainer.asIterable().forEach {
            it.onSectionChanged(oldSection, newSection)
        }
    }
}
