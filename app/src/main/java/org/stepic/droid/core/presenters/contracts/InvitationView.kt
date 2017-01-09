package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Course

interface InvitationView {
    fun onShowInvitationDialog(course: Course)
}

