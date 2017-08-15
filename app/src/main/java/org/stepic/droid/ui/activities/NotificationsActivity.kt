package org.stepic.droid.ui.activities

import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.NotificationsFragment

class NotificationsActivity : SingleFragmentActivity() {
    override fun createFragment() = NotificationsFragment.newInstance()
}