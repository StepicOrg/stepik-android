package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.code.ProgrammingLanguage

interface CodeView {
    fun onAttemptIsNotStored()
    fun onShowStored(programmingLanguage: ProgrammingLanguage, code: String)
}
