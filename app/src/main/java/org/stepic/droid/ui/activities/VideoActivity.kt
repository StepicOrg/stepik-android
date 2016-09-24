package org.stepic.droid.ui.activities

import android.support.v4.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.VideoFragment
import timber.log.Timber

class VideoActivity : SingleFragmentActivity() {
    companion object {
        val videoPathKey = "VIDEO_URI_KEY"
        val videoIdKey = "VIDEO_ID_KEY"
    }

    override fun createFragment(): Fragment? {
        val path: String? = intent.extras.getString(videoPathKey)
        val videoId : Long = intent.extras.getLong(videoIdKey)
        if (path != null) {
            return VideoFragment.newInstance(path, videoId)
        } else {
            return null
        }
    }

    override fun finish() {
        Timber.d("finish")
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }
}