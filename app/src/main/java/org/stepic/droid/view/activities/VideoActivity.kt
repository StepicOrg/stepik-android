package org.stepic.droid.view.activities

import android.support.v4.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.view.fragments.VideoFragment

class VideoActivity : SingleFragmentActivity() {
    companion object {
        val videoPathKey = "VIDEO_URI_KEY"
    }

    override fun createFragment(): Fragment? {
        val path: String? = intent.extras.getString(videoPathKey)
        if (path != null) {
            return VideoFragment.newInstance(path)
        } else {
            return null
        }
    }
}