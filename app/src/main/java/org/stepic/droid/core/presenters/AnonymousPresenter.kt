package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.AnonymousView
import org.stepic.droid.preferences.SharedPreferenceHelper
import java.util.concurrent.ThreadPoolExecutor

class AnonymousPresenter(private val sharedPreferenceHelper: SharedPreferenceHelper,
                         private val threadPoolExecutor: ThreadPoolExecutor,
                         private val mainHandler: IMainHandler) : PresenterBase<AnonymousView>() {

    fun checkForAnonymous() {
        threadPoolExecutor.execute {
            val isAnonymous = sharedPreferenceHelper.authResponseFromStore == null
            mainHandler.post {
                view?.onShowAnonymous(isAnonymous)
            }
        }
    }
}
