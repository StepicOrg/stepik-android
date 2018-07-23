package org.stepic.droid.core.routing.contract

import org.stepik.android.model.Section

interface RoutingListener {
    fun onSectionChanged(oldSection: Section, newSection: Section)
}
