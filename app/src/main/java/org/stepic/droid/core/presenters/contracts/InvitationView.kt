package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.Course

interface InvitationView {
    fun onShowInvitationDialog(course: Course)
}

