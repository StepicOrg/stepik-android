package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.structure.Course

interface InvitationView {
    fun onShowInvitationDialog(course: Course)
}

