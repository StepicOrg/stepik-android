package org.stepic.droid.core.routing.contract

import org.stepic.droid.model.Section

interface RoutingListener {
    fun onSectionChanged(oldSection: Section, newSection: Section)
}
