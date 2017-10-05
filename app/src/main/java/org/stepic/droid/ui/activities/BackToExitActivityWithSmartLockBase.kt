package org.stepic.droid.ui.activities

abstract class BackToExitActivityWithSmartLockBase : SmartLockActivityBase() {
    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }
}