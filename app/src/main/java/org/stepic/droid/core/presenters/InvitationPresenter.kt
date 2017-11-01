package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.InvitationView
import org.stepic.droid.di.course.CourseAndSectionsScope
import org.stepic.droid.model.Course
import javax.inject.Inject

@CourseAndSectionsScope
class InvitationPresenter
@Inject constructor(
        private val analytic: Analytic) : PresenterBase<InvitationView>() {

    fun needShowInvitationDialog(course: Course) {
        analytic.reportEvent(Analytic.Interaction.SHOW_MATERIAL_DIALOG_INVITATION)
        view?.onShowInvitationDialog(course)
    }

}
