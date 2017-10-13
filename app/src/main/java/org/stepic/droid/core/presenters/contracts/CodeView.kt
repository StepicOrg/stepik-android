package org.stepic.droid.core.presenters.contracts

interface CodeView {
    fun onAttemptIsNotStored()

    fun onShowStored(language: String, code: String)
}
