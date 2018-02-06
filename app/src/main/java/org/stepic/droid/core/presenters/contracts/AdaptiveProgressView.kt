package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.adaptive.ui.adapters.AdaptiveWeeksAdapter

interface AdaptiveProgressView {
    fun onWeeksAdapter(adapter: AdaptiveWeeksAdapter)
}