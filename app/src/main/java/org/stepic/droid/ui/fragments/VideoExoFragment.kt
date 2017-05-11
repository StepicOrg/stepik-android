package org.stepic.droid.ui.fragments

import android.content.Context.TELEPHONY_SERVICE
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ExoPlayer.STATE_READY
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
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.MyExoPhoneStateListener
import org.stepic.droid.core.presenters.VideoWithTimestampPresenter
import org.stepic.droid.core.presenters.contracts.VideoWithTimestampView
import org.stepic.droid.ui.custom_exo.NavigationBarUtil
import org.stepic.droid.ui.util.VideoPlayerConstants


class VideoExoFragment : FragmentBase(),
        ExoPlayer.EventListener,
        SimpleExoPlayer.VideoListener,
        AudioManager.OnAudioFocusChangeListener,
        VideoWithTimestampView,
        MyExoPhoneStateListener.Callback {

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
        when (playbackState) {
            STATE_READY ->
                if (playWhenReady) {
                    //now we click "play"
                    audioFocusHelper.requestAudioFocus(this)
                } else {
                    //pause clicked
                    audioFocusHelper.releaseAudioFocus(this)
                }
        }
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
        private val VIDEO_PATH_KEY = "video_path_key"
        private val VIDEO_ID_KEY = "video_id_key"

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
    private lateinit var videoWithTimestampPresenter: VideoWithTimestampPresenter
    private lateinit var mediaSource: ExtractorMediaSource

    override fun injectComponent() {
        App
                .component()
                .videoComponentBuilder()
                .build()
                .inject(this)
    }

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
        videoPlayerView?.controllerShowTimeoutMs = VideoPlayerConstants.TIMEOUT_BEFORE_HIDE
        videoPlayerView?.setFastForwardIncrementMs(VideoPlayerConstants.JUMP_TIME_MILLIS)
        videoPlayerView?.setRewindIncrementMs(VideoPlayerConstants.JUMP_TIME_MILLIS)

        audioFocusHelper.requestAudioFocus(this)
        videoPlayerView?.player = player
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        player?.prepare(mediaSource)
        videoPlayerView?.showController()
        videoWithTimestampPresenter.attachView(this)
    }

    override fun onResume() {
        super.onResume()
        exoPhoneListener.subscribe(this)

        val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(exoPhoneListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun onPause() {
        super.onPause()

        val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(exoPhoneListener, PhoneStateListener.LISTEN_NONE)
        exoPhoneListener.unsubscribe()

        releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        videoWithTimestampPresenter.detachView(this)
        videoPlayerView?.setControllerVisibilityListener(null)
    }

    private fun releasePlayer() {
        audioFocusHelper.releaseAudioFocus(this)
        player?.release()
        player = null
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
//            AudioManager.AUDIOFOCUS_GAIN ->  //do nothing?
            AudioManager.AUDIOFOCUS_LOSS -> onAudioFocusLoss()
        }
    }

    private fun onAudioFocusLoss() {
        pausePlayer()
    }

    override fun onIncomingCall() {
        pausePlayer()
    }


    private fun pausePlayer() {
        player?.playWhenReady = false // pause player
    }

    override fun onNeedShowVideoWithTimestamp(timestamp: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
