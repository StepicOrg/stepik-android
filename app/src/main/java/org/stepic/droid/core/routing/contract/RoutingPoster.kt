package org.stepic.droid.core.routing.contract

import org.stepic.droid.model.Section

interface RoutingPoster {
    fun sectionChanged(oldSection: Section, newSection: Section)
}
