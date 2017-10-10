package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.CodeView
import org.stepic.droid.di.step.code.CodeScope
import org.stepic.droid.storage.operations.DatabaseFacade
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CodeScope
class CodePresenter
@Inject constructor(
        private val mainHandler: MainHandler,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val databaseFacade: DatabaseFacade
) : PresenterBase<CodeView>() {

    fun onShowAttempt(attemptId: Long, stepId: Long) {
        //if we have code for attemptId in database -> show
        //otherwise -> get by stepId and remove all -> show from block
        threadPoolExecutor.execute {
            val codeSubmission = databaseFacade.getCodeSubmission(attemptId)
            if (codeSubmission == null) {
                mainHandler.post {
                    view?.onAttemptIsNotStored()
                }
            } else {
                mainHandler.post {
                    view?.onShowStored(codeSubmission.language, codeSubmission.code)
                }
            }

        }
    }
}
