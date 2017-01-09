package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.InvitationView
import org.stepic.droid.model.Course
import org.stepic.droid.preferences.SharedPreferenceHelper
import java.util.concurrent.ThreadPoolExecutor

class InvitationPresenter(private val threadPoolExecutor: ThreadPoolExecutor,
                          private val mainHandler: IMainHandler,
                          private val sharedPreferenceHelper: SharedPreferenceHelper,
                          private val analytic: Analytic) : PresenterBase<InvitationView>() {

    fun needShowInvitationDialog(course: Course) {
        threadPoolExecutor.execute {
            if (sharedPreferenceHelper.isInvitationWasDeclined) {
                analytic.reportEvent(Analytic.Interaction.INVITATION_PREVENTED)
            } else {
                analytic.reportEvent(Analytic.Interaction.SHOW_MATERIAL_DIALOG_INVITATION)
                mainHandler.post { view?.onShowInvitationDialog(course) }
            }
        }
    }


    fun onClickDecline() {
        threadPoolExecutor.execute {
            analytic.reportEvent(Analytic.Interaction.NEGATIVE_MATERIAL_DIALOG_INVITATION)
            sharedPreferenceHelper.onDeclineInvitation()
        }
    }

}
