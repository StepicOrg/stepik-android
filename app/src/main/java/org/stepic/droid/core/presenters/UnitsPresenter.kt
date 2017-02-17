package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.UnitsView
import org.stepic.droid.notifications.LocalReminder
import org.stepic.droid.preferences.SharedPreferenceHelper
import java.util.concurrent.ThreadPoolExecutor

class UnitsPresenter(private val analytic: Analytic,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: IMainHandler,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val localReminder: LocalReminder) : PresenterBase<UnitsView>() {

}
