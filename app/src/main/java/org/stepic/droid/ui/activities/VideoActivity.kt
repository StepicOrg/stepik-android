package org.stepic.droid.ui.activities

import android.support.v4.app.Fragment
import android.view.KeyEvent
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.model.Video
import org.stepic.droid.ui.fragments.VideoExoFragment
import org.stepic.droid.ui.listeners.KeyDispatchableFragment

class VideoActivity : SingleFragmentActivity() {
    companion object {
        val videoPathKey = "VIDEO_URI_KEY"
        val videoIdKey = "VIDEO_ID_KEY"

        val cachedVideoKey = "cached_video_key"
        val externalVideoKey = "external_video_key"
    }

    override fun createFragment(): Fragment? {
        val path: String? = intent.extras.getString(videoPathKey)
        val videoId: Long = intent.extras.getLong(videoIdKey)
        val cachedVideo = intent.extras.getParcelable<Video>(cachedVideoKey)
        val externalVideo = intent.extras.getParcelable<Video>(externalVideoKey)
        if (externalVideo != null) {
            return VideoExoFragment.newInstance(
                    cachedVideo = cachedVideo,
                    externalVideo = externalVideo
            )
        } else if (path != null) {
            return VideoExoFragment.newInstance(path, videoId)
        } else {
            return null
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }

    override fun onBackPressed() {
        this.finish()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // If the event was not handled then see if the player view can handle it as a media key event.
        return super.dispatchKeyEvent(event) || (fragment as KeyDispatchableFragment).dispatchKeyEventInFragment(event)
    }

}