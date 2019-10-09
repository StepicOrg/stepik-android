package org.stepic.droid.ui.activities

import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.DownloadsFragment

class DownloadsActivity : SingleFragmentActivity() {
    override fun createFragment(): DownloadsFragment =
        DownloadsFragment.newInstance()

    override fun applyTransitionPrev() {
        //stub, do not add something
    }
}
