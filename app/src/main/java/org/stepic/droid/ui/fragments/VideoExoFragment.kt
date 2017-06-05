package org.stepic.droid.ui.fragments

import android.content.Context.TELEPHONY_SERVICE
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ExoPlayer.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.exo_playback_control_view.*
import kotlinx.android.synthetic.main.fragment_exo_video.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.Client
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.MyExoPhoneStateListener
import org.stepic.droid.core.internet_state.contract.InternetEnabledListener
import org.stepic.droid.core.presenters.VideoWithTimestampPresenter
import org.stepic.droid.core.presenters.contracts.VideoWithTimestampView
import org.stepic.droid.model.Video
import org.stepic.droid.preferences.VideoPlaybackRate
import org.stepic.droid.receivers.HeadPhoneReceiver
import org.stepic.droid.ui.custom_exo.NavigationBarUtil
import org.stepic.droid.ui.listeners.KeyDispatchableFragment
import org.stepic.droid.ui.util.VideoPlayerConstants
import org.stepic.droid.util.resolvers.VideoResolver
import javax.inject.Inject

/**
 * we use onStart/onStop for starting playing and releasing playerLayout (instead of onResume/onPause) for supporting Api23+ Multi-Window mode.
 * We can't split it by api version, because Samsung's Tablets Api21+ can support Multi Window feature
 */
class VideoExoFragment : FragmentBase(),
        ExoPlayer.EventListener,
        SimpleExoPlayer.VideoListener,
        AudioManager.OnAudioFocusChangeListener,
        VideoWithTimestampView,
        MyExoPhoneStateListener.Callback,
        InternetEnabledListener,
        HeadPhoneReceiver.HeadPhoneListener,
        KeyDispatchableFragment {

    override fun dispatchKeyEventInFragment(keyEvent: KeyEvent?): Boolean {
        videoPlayerView?.showController()
        return videoPlayerView?.dispatchMediaKeyEvent(keyEvent) ?: false;
    }

    override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
    }

    override fun onRenderedFirstFrame() {
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        if (error?.type == ExoPlaybackException.TYPE_SOURCE && error.cause is HttpDataSource.HttpDataSourceException) {
            if (activity != null) {
                Toast.makeText(activity, R.string.no_connection, Toast.LENGTH_SHORT).show()
            }
        } else if (error != null) {
            analytic.reportError(Analytic.Video.ERROR, error)
        }
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
            STATE_ENDED -> {
                videoWithTimestampPresenter.saveMillis(0L, videoId)
                activity?.finish()
            }
        }
    }

    override fun onLoadingChanged(isLoading: Boolean) {
    }

    override fun onPositionDiscontinuity() {
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
    }

    var cachedVideo: Video? = null
    var externalVideo: Video? = null

    companion object {
        private val VIDEO_PATH_KEY = "video_path_key"
        private val VIDEO_ID_KEY = "video_id_key"

        private val CACHED_VIDEO_KEY = "cached_video_key"
        private val EXTERNAL_VIDEO_KEY = "external_video_key"

        private val saveEpsilon = 1000L

        fun newInstance(videoUri: String, videoId: Long): VideoExoFragment {
            val args = Bundle()
            args.putString(VIDEO_PATH_KEY, videoUri)
            args.putLong(VIDEO_ID_KEY, videoId)
            val fragment = VideoExoFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(cachedVideo: Video?, externalVideo: Video): VideoExoFragment {
            val args = Bundle()
            args.putParcelable(CACHED_VIDEO_KEY, cachedVideo)
            args.putParcelable(EXTERNAL_VIDEO_KEY, externalVideo)
            args.putLong(VIDEO_ID_KEY, cachedVideo?.id ?: externalVideo.id)
            val fragment = VideoExoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var player: SimpleExoPlayer? = null

    @Inject
    lateinit var videoWithTimestampPresenter: VideoWithTimestampPresenter
    @Inject
    lateinit var videoResolver: VideoResolver

    private var autoPlay: Boolean = false
    private var videoId: Long? = null
    private var mediaSource: ExtractorMediaSource? = null
    private val headPhoneReceiver: HeadPhoneReceiver = HeadPhoneReceiver()

    @Inject
    lateinit var internetEnabledClient: Client<InternetEnabledListener>

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
        autoPlay = true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_exo_video, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoPlayerView.setControllerVisibilityListener { visibility ->
            if (visibility == View.VISIBLE) {
                NavigationBarUtil.hideNavigationBar(false, activity)
            } else if (visibility == View.GONE) {
                NavigationBarUtil.hideNavigationBar(true, activity)
            }
        }
        videoPlayerView.controllerShowTimeoutMs = VideoPlayerConstants.TIMEOUT_BEFORE_HIDE
        videoPlayerView.setFastForwardIncrementMs(VideoPlayerConstants.JUMP_TIME_MILLIS)
        videoPlayerView.setRewindIncrementMs(VideoPlayerConstants.JUMP_TIME_MILLIS)
        videoPlayerView.requestFocus()
        closeButton.setOnClickListener {
            activity?.finish()
        }
        videoRateChooser.setOnClickListener {
            showChooseRateMenu(it)
        }

        moreItemsView.setOnClickListener {
            showMoreItemsPopup(it)
        }

        videoWithTimestampPresenter.attachView(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val intentFilter = IntentFilter()
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        activity.registerReceiver(headPhoneReceiver, intentFilter)
        headPhoneReceiver.listener = this
    }

    override fun onStart() {
        super.onStart()
        // if it is not in locked screen
        videoId = createPlayer()
        videoWithTimestampPresenter.showVideoWithPredefinedTimestamp(videoId)

        internetEnabledClient.subscribe(this)
        exoPhoneListener.subscribe(this)

        val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(exoPhoneListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun onStop() {
        super.onStop()

        val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(exoPhoneListener, PhoneStateListener.LISTEN_NONE)
        exoPhoneListener.unsubscribe()
        internetEnabledClient.unsubscribe(this)
        releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        headPhoneReceiver.listener = null
        activity?.unregisterReceiver(headPhoneReceiver)
        videoWithTimestampPresenter.detachView(this)
        videoPlayerView?.setControllerVisibilityListener(null)
    }

    private fun createPlayer(): Long? {
        videoPlayerView?.hideController()


        cachedVideo = arguments.getParcelable<Video>(CACHED_VIDEO_KEY)
        externalVideo = arguments.getParcelable<Video>(EXTERNAL_VIDEO_KEY)

        val video = cachedVideo ?: externalVideo

        var videoId: Long? = video?.id
        var filePath: String? = videoResolver.resolveVideoUrl(video)

        if (videoId == null || filePath == null) {
            filePath = arguments.getString(VIDEO_PATH_KEY)
            videoId = arguments.getLong(VIDEO_ID_KEY)
            if (videoId <= 0L) { // if equal zero -> it is default, it is not our video
                videoId = null
            }
        }
        val bandwidthMeter = DefaultBandwidthMeter()

        //make source
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "Stepik"), bandwidthMeter)
        val extractorsFactory = DefaultExtractorsFactory()
        mediaSource = ExtractorMediaSource(Uri.parse(filePath), dataSourceFactory, extractorsFactory, null, null)

        val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(adaptiveTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        player?.addListener(this)
        player?.setVideoListener(this)

        audioFocusHelper.requestAudioFocus(this)
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT

        val videoPlaybackRate = userPreferences.videoPlaybackRate //this call from SharedPreferences, todo: make it from background thread
        setVideoRate(videoPlaybackRate)

        val alwaysRotate = userPreferences.isRotateVideo
        setOrientationPreference(alwaysRotate) // todo: make userPrefs call async

        analytic.reportEventWithName(Analytic.Video.PLAYER_CREATED, alwaysRotate.toString())//item_name=true/false – rotate flag indication

        return videoId
    }


    override fun onNeedShowVideoWithTimestamp(timestamp: Long) {
        player?.seekTo(timestamp)
        if (autoPlay) {
            player?.playWhenReady = true
            autoPlay = false
        } else {
            player?.playWhenReady = false
        }
        videoPlayerView?.player = player
        videoPlayerView?.showController()
        player?.prepare(mediaSource, false, false)
    }


    private fun setOrientationPreference(alwaysRotate: Boolean) {
        if (alwaysRotate) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun setVideoRate(videoPlaybackRate: VideoPlaybackRate) {
        player?.playbackParameters = PlaybackParameters(videoPlaybackRate.rateFloat, 1f)
        videoRateChooser?.setImageDrawable(videoPlaybackRate.icon)
    }

    private fun releasePlayer() {
        val currentPosition = player?.currentPosition
        val duration = player?.duration
        if (currentPosition != null && duration != null) {
            //save only when info of time is exist
            if (duration > 0 && currentPosition + saveEpsilon >= duration) {
                //end of the video
                videoWithTimestampPresenter.saveMillis(0, videoId)
            } else {
                videoWithTimestampPresenter.saveMillis(currentPosition, videoId)
            }
        }
        audioFocusHelper.releaseAudioFocus(this)
        player?.removeListener(this)
        player?.setVideoListener(null)
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
        player?.playWhenReady = false // pause playerLayout
    }

    private fun showChooseRateMenu(view: View) {
        analytic.reportEvent(Analytic.Video.SHOW_CHOOSE_RATE)
        val popupMenu = PopupMenu(App.getAppContext(), view)
        popupMenu.inflate(R.menu.video_rate_menu)
        popupMenu.setOnMenuItemClickListener {
            val rate = VideoPlaybackRate.getValueById(it.itemId)
            setVideoRate(rate)
            userPreferences.videoPlaybackRate = rate
            true
        }
        popupMenu.show()
    }

    private fun showMoreItemsPopup(view: View) {
        if (activity == null) {
            return
        }

        analytic.reportEvent(Analytic.Video.SHOW_MORE_ITEMS)

        val morePopupMenu = PopupMenu(activity, view)
        morePopupMenu.inflate(R.menu.video_more_menu)

        val shouldRotate = userPreferences.isRotateVideo
        val menuItem = morePopupMenu.menu.findItem(R.id.orientation_flag)
        menuItem.isChecked = shouldRotate

        morePopupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.orientation_flag -> {
                    analytic.reportEvent(Analytic.Video.ROTATE_CLICKED)

                    val oldValue = it.isChecked
                    val newValue = !oldValue
                    it.isChecked = newValue
                    userPreferences.isRotateVideo = newValue

                    setOrientationPreference(newValue)
                    true
                }
                else -> false

            }
        }

        morePopupMenu.show()
    }

    override fun onInternetEnabled() {
        if (player != null && player?.playbackState == STATE_IDLE) {
            releasePlayer()

            autoPlay = true
            videoId = createPlayer()
            videoWithTimestampPresenter.showVideoWithPredefinedTimestamp(videoId)
        }
    }

    override fun onUnplugHeadphones() {
        pausePlayer()
    }


}
