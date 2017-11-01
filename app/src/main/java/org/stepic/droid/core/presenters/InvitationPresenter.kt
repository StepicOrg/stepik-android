package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.InvitationView
import org.stepic.droid.di.course.CourseAndSectionsScope
import org.stepic.droid.model.Course
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CourseAndSectionsScope
class InvitationPresenter
@Inject constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val analytic: Analytic) : PresenterBase<InvitationView>() {

    fun needShowInvitationDialog(course: Course) {
        threadPoolExecutor.execute {
            analytic.reportEvent(Analytic.Interaction.SHOW_MATERIAL_DIALOG_INVITATION)
            mainHandler.post { view?.onShowInvitationDialog(course) }
        }
    }

}
