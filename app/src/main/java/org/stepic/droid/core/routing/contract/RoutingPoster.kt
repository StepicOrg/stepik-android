package org.stepic.droid.core.routing.contract

import org.stepik.android.model.Section

interface RoutingPoster {
    fun sectionChanged(oldSection: Section, newSection: Section)
}
