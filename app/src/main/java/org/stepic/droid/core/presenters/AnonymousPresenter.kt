package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.AnonymousView
import org.stepic.droid.preferences.SharedPreferenceHelper
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class AnonymousPresenter
@Inject constructor(
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler) : PresenterBase<AnonymousView>() {

    fun checkForAnonymous() {
        threadPoolExecutor.execute {
            val isAnonymous = sharedPreferenceHelper.authResponseFromStore == null
            mainHandler.post {
                view?.onShowAnonymous(isAnonymous)
            }
        }
    }
}
