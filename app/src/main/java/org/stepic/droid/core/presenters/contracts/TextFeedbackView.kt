package org.stepic.droid.core.presenters.contracts

interface TextFeedbackView {
    fun onFeedbackSent()

    fun onServerFail()

    fun onInternetProblems()
}
