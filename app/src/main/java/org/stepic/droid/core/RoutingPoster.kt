package org.stepic.droid.core

import org.stepic.droid.model.Section

interface RoutingPoster {
    fun onSectionChanged(oldSection: Section, newSection: Section)
}
