package org.stepic.droid.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.fragment_exo_video.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.ui.custom_exo.NavigationBarUtil


class VideoExoFragment : FragmentBase(), ExoPlayer.EventListener, SimpleExoPlayer.VideoListener {
    override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRenderedFirstFrame() {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoadingChanged(isLoading: Boolean) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPositionDiscontinuity() {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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
    private var player: SimpleExoPlayer? = null

    private lateinit var mediaSource: ExtractorMediaSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        filePath = arguments.getString(VIDEO_PATH_KEY)
        videoId = arguments.getLong(VIDEO_ID_KEY)
        if (videoId != null && videoId!! <= 0L) { // if equal zero -> it is default, it is not our video
            videoId = null
        }
        val bandwidthMeter = DefaultBandwidthMeter()
        val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(adaptiveTrackSelectionFactory)
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "Stepik"), bandwidthMeter)
        val extractorsFactory = DefaultExtractorsFactory()

        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        player?.addListener(this)

        val eventLogger = EventLogger(trackSelector)
        player?.addListener(eventLogger)
        player?.setAudioDebugListener(eventLogger)
        player?.setVideoDebugListener(eventLogger)
        player?.setMetadataOutput(eventLogger)

        player?.setVideoListener(this)

        mediaSource = ExtractorMediaSource(Uri.parse(filePath), dataSourceFactory, extractorsFactory, null, null)

        player?.playWhenReady = true
//        player?.blockingSendMessages()

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_exo_video, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoPlayerView?.setControllerVisibilityListener { visivility ->
            if (visivility == View.VISIBLE) {
                NavigationBarUtil.hideNavigationBar(false, activity)
            } else if (visivility == View.GONE) {
                NavigationBarUtil.hideNavigationBar(true, activity)
            }
        }

        videoPlayerView?.player = player
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        player?.prepare(mediaSource)
        videoPlayerView?.showController()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        videoPlayerView?.setControllerVisibilityListener(null)
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

}
