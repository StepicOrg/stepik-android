package org.stepic.droid.core

import org.stepic.droid.model.Section

interface RoutingConsumer {
    fun subscribe(listener: Listener)

    fun unsubscribe(listener: Listener)

    interface Listener {
        fun onSectionChanged(oldSection: Section, newSection: Section)
    }
}
