package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.User

interface InstructorsView {

    fun onLoadingInstructors()

    fun onFailLoadInstructors()

    fun onInstructorsLoaded(users: List<User>)

    fun onHideInstructors()

}
