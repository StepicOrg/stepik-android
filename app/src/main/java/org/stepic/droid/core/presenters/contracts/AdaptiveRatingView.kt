package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.adaptive.ui.adapters.AdaptiveRatingAdapter

interface AdaptiveRatingView {
    fun onLoading()
    fun onConnectivityError()
    fun onRequestError()
    fun onComplete()
    fun onRatingAdapter(adapter: AdaptiveRatingAdapter)
}