package org.stepic.droid.core.presenters.contracts

interface UpdateAppView {
    fun onNeedUpdate(linkForUpdate: String?, isAppInGp: Boolean)
}
