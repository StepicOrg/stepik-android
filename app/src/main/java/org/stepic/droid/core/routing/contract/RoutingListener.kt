package org.stepic.droid.core.routing.contract

import org.stepik.android.model.structure.Section

interface RoutingListener {
    fun onSectionChanged(oldSection: Section, newSection: Section)
}
