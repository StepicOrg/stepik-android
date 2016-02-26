package org.stepic.droid.view.activities

import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.view.fragments.VideoFragment

class VideoActivity : SingleFragmentActivity() {
    override fun createFragment() = VideoFragment.newInstance()
}