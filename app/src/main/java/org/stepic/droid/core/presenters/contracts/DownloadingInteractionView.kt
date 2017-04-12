package org.stepic.droid.core.presenters.contracts

interface DownloadingInteractionView {

    fun onLoadingAccepted(position: Int)

    fun onShowPreferenceSuggestion()

    fun onShowInternetIsNotAvailableRetry(position: Int)

}
