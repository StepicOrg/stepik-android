package org.stepic.droid.ui.fragments

import android.os.Bundle
import org.stepic.droid.base.FragmentBase

class VideoExoFragment : FragmentBase() {
    companion object {
        private val TIMEOUT_BEFORE_HIDE = 4500L
        private val INDEX_PLAY_IMAGE = 0
        private val INDEX_PAUSE_IMAGE = 1
        private val JUMP_TIME_MILLIS = 10000L
        private val JUMP_MAX_DELTA = 3000L
        private val VIDEO_PATH_KEY = "video_path_key"
        private val VIDEO_ID_KEY = "video_id_key"
        private val DELTA_TIME = 0L

        fun newInstance(videoUri: String, videoId: Long): VideoExoFragment {
            val args = Bundle()
            args.putString(VIDEO_PATH_KEY, videoUri)
            args.putLong(VIDEO_ID_KEY, videoId)
            val fragment = VideoExoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var filePath: String
    private var videoId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        filePath = arguments.getString(VIDEO_PATH_KEY)
        videoId = arguments.getLong(VIDEO_ID_KEY)
        if (videoId != null && videoId!! <= 0L) { // if equal zero -> it is default, it is not our video
            videoId = null
        }
//        initPhoneStateListener()
//        isOnResumeDirectlyAfterOnCreate = true
    }


}
