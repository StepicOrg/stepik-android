package org.stepik.android.view.video_player.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.GestureDetector
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.MotionEvent
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.exo_player_control_view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.preferences.VideoPlaybackRate
import org.stepic.droid.ui.custom_exo.NavigationBarUtil
import org.stepic.droid.ui.dialogs.VideoQualityDialogInPlayer
import org.stepic.droid.ui.util.PopupHelper
import org.stepic.droid.util.DisplayUtils
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.domain.video_player.analytic.PIPActivated
import org.stepik.android.domain.video_player.analytic.VideoPlayerControlClickedEvent
import org.stepik.android.model.Video
import org.stepik.android.model.VideoUrl
import org.stepik.android.presentation.video_player.VideoPlayerPresenter
import org.stepik.android.presentation.video_player.VideoPlayerView
import org.stepik.android.view.lesson.ui.activity.LessonActivity
import org.stepik.android.view.notification.extension.PendingIntentCompat
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import org.stepik.android.view.video_player.model.VideoPlayerData
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import org.stepik.android.view.video_player.ui.service.VideoPlayerForegroundService
import javax.inject.Inject

class VideoPlayerActivity : AppCompatActivity(), VideoPlayerView, VideoQualityDialogInPlayer.Callback {
    companion object {
        /***
         *  Picture-in-picture related
         */
        private const val ACTION_MEDIA_CONTROL = "media_control"

        private const val EXTRA_CONTROL_TYPE = "control_type"

        private const val REQUEST_PLAY = 1
        private const val REQUEST_PAUSE = 2
        private const val REQUEST_REWIND = 3
        private const val REQUEST_FORWARD = 4

        private const val CONTROL_TYPE_PLAY = 1
        private const val CONTROL_TYPE_PAUSE = 2
        private const val CONTROL_TYPE_REWIND = 3
        private const val CONTROL_TYPE_FORWARD = 4

        /***
         * Video player related
         */
        const val REQUEST_CODE = 1535

        private const val TIMEOUT_BEFORE_HIDE = 4000
        private const val JUMP_TIME_MILLIS = 10000

        private const val IN_BACKGROUND_POPUP_TIMEOUT_MS = 3000L

        private const val AUTOPLAY_PROGRESS_MAX = 3600
        private const val AUTOPLAY_ANIMATION_DURATION_MS = 7200L

        private const val EXTRA_VIDEO_PLAYER_DATA = "video_player_media_data"
        private const val EXTRA_LESSON_DATA = "lesson_data"

        fun createIntent(context: Context, videoPlayerMediaData: VideoPlayerMediaData, lessonData: LessonData? = null): Intent =
            Intent(context, VideoPlayerActivity::class.java)
                .putExtra(EXTRA_VIDEO_PLAYER_DATA, videoPlayerMediaData)
                .putExtra(EXTRA_LESSON_DATA, lessonData)
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private var isPlaying: Boolean = false
        set(value) {
            // When seeking in video, isPlaying get set to `true` twice
            if (field != value) {
                logIsPlayingEvent(value)
            }
            field = value
        }

    private val lessonMoveNextIntent: Intent? by lazy {
        intent.getParcelableExtra<LessonData>(EXTRA_LESSON_DATA)?.let { lessonData ->
            LessonActivity
                .createIntent(this, lessonData)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    private val isAutoplayEnabled: Boolean by lazy { lessonMoveNextIntent != null }
    private val screenMiddlePointX = DisplayUtils.getScreenWidth() / 2

    private lateinit var labelPlay: String
    private lateinit var labelPause: String
    private lateinit var labelRewind: String
    private lateinit var labelForward: String

    private val videoServiceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                if (service is VideoPlayerForegroundService.VideoPlayerBinder) {
                    exoPlayer = service.getPlayer()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                exoPlayer = null
            }
        }

    private val exoPlayerListener =
        object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                if (error.cause is HttpDataSource.HttpDataSourceException) {
                    Toast
                        .makeText(this@VideoPlayerActivity, R.string.no_connection, Toast.LENGTH_LONG)
                        .show()

                    analytic.reportError(Analytic.Video.CONNECTION_ERROR, error)
                } else {
                    analytic.reportError(Analytic.Video.ERROR, error)
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isSupportPIP()) {
                    return
                }
                if (isPlaying) {
                    updatePictureInPictureActions(R.drawable.ic_pause_24, labelPause, CONTROL_TYPE_PAUSE, REQUEST_PAUSE)
                } else {
                    updatePictureInPictureActions(R.drawable.ic_play_arrow_24, labelPlay, CONTROL_TYPE_PLAY, REQUEST_PLAY)
                }
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when {
                    playbackState == Player.STATE_READY && playWhenReady ->
                        isPlaying = true
                    playbackState == Player.STATE_READY && !playWhenReady ->
                        isPlaying = false
                }
                videoPlayerPresenter.onPlayerStateChanged(playbackState, isAutoplayEnabled)
            }
        }

    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                if (intent.action != ACTION_MEDIA_CONTROL) {
                    return
                }
                when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                    CONTROL_TYPE_PLAY -> {
                        if (exoPlayer?.playbackState == Player.STATE_ENDED) {
                            exoPlayer?.seekTo(0)
                        }
                        exoPlayer?.playWhenReady = true
                    }
                    CONTROL_TYPE_PAUSE -> exoPlayer?.playWhenReady = false
                    CONTROL_TYPE_REWIND -> exoPlayer?.let {
                        it.seekTo(it.currentPosition - JUMP_TIME_MILLIS)
                    }
                    CONTROL_TYPE_FORWARD -> exoPlayer?.let {
                        it.seekTo(it.currentPosition + JUMP_TIME_MILLIS)
                    }
                }
            }
        }
    }

    private lateinit var gestureDetector: GestureDetectorCompat

    private val onSimpleGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val action = if (e.rawX > screenMiddlePointX) {
                VideoPlayerControlClickedEvent.ACTION_DOUBLE_CLICK_RIGHT
            } else {
                VideoPlayerControlClickedEvent.ACTION_DOUBLE_CLICK_LEFT
            }
            analytic.report(VideoPlayerControlClickedEvent(action))
            return super.onDoubleTap(e)
        }
    }

    private var exoPlayer: Player? = null
        set(value) {
            field?.removeListener(exoPlayerListener)
            field = value

            if (value != null) {
                value.addListener(exoPlayerListener)

                videoPlayerPresenter.onPlayerStateChanged(value.playbackState, isAutoplayEnabled)
            }

            playerView?.player = value
        }
    private var isLandscapeVideo = false
    private var isPIPModeActive = false

    private val videoPlayerPresenter: VideoPlayerPresenter by viewModels { viewModelFactory }
    private lateinit var viewStateDelegate: ViewStateDelegate<VideoPlayerView.State>

    private var animator: ValueAnimator? = null

    private var playerInBackroundPopup: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        gestureDetector = GestureDetectorCompat(this, onSimpleGestureListener)

        labelPlay = getString(R.string.pip_play_label)
        labelPause = getString(R.string.pip_stop_label)
        labelRewind = getString(R.string.pip_rewind_label)
        labelForward = getString(R.string.pip_forward_label)

        savedInstanceState
            ?.let(videoPlayerPresenter::onRestoreInstanceState)

        intent
            ?.getParcelableExtra<VideoPlayerMediaData>(EXTRA_VIDEO_PLAYER_DATA)
            ?.let { videoPlayerPresenter.onMediaData(it, isFromNewIntent = false) }

        setTitle(R.string.video_title)
        setContentView(R.layout.activity_video_player)

        closeButton.setOnClickListener {
            finish()
        }

        videoRateChooser.setOnClickListener {
            showChooseRateMenu(it)
        }
        playerView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()
            }
            gestureDetector.onTouchEvent(event)
            true
        }

        qualityView.isVisible = false
        playerView.controllerShowTimeoutMs = TIMEOUT_BEFORE_HIDE

        exo_pip_icon_container.isVisible = isSupportPIP()
        exo_pip_icon_container.setOnClickListener {
            analytic.report(PIPActivated())
            enterPipMode()
        }
        exo_fullscreen_icon_container.setOnClickListener { changeVideoRotation() }

        playerView.setControllerVisibilityListener { visibility ->
            if (isSupportPIP() && isInPictureInPictureMode) {
                playerView.hideController()
                return@setControllerVisibilityListener
            }
            if (visibility == View.VISIBLE) {
                NavigationBarUtil.hideNavigationBar(false, this)
            } else if (visibility == View.GONE) {
                NavigationBarUtil.hideNavigationBar(true, this)
            }
        }

        autoplay_controller_panel.setOnClickListener {
            move(StepNavigationDirection.NEXT)
        }
        autoplayProgress.max = AUTOPLAY_PROGRESS_MAX
        autoplayCancel.setOnClickListener { videoPlayerPresenter.stayOnThisStep() }
        autoplaySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (autoplaySwitch.isUserTriggered) {
                videoPlayerPresenter.setAutoplayEnabled(isChecked)
            }
        }

        rewind.setOnClickListener {
            exoPlayer?.let { player -> player.seekTo(player.currentPosition - JUMP_TIME_MILLIS) }
        }

        forward.setOnClickListener {
            exoPlayer?.let { player -> player.seekTo(player.currentPosition + JUMP_TIME_MILLIS) }
        }

        skip_prev.setOnClickListener {
            analytic.report(VideoPlayerControlClickedEvent(VideoPlayerControlClickedEvent.ACTION_PREVIOS))
            move(StepNavigationDirection.PREV)
        }

        skip_next.setOnClickListener {
            analytic.report(VideoPlayerControlClickedEvent(VideoPlayerControlClickedEvent.ACTION_NEXT))
            move(StepNavigationDirection.NEXT)
        }

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<VideoPlayerView.State.Idle>(center_controller_panel)
        viewStateDelegate.addState<VideoPlayerView.State.AutoplayPending>(autoplay_controller_panel, autoplayCancel, autoplaySwitch)
        viewStateDelegate.addState<VideoPlayerView.State.AutoplayCancelled>(autoplay_controller_panel, autoplayCancel, autoplaySwitch)
    }

    private fun injectComponent() {
        App.component()
            .videoPlayerComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent
            ?.getParcelableExtra<VideoPlayerMediaData>(EXTRA_VIDEO_PLAYER_DATA)
            ?.let { videoPlayerPresenter.onMediaData(it, isFromNewIntent = true) }
    }

    override fun invalidatePlayer() {
        exoPlayer?.stop(true)
    }

    override fun onStart() {
        super.onStart()
        videoPlayerPresenter.attachView(this)
        bindService(VideoPlayerForegroundService.createBindingIntent(this), videoServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        playerInBackroundPopup?.dismiss()
        super.onPause()
    }

    override fun onStop() {
        if (isSupportPIP() && isPIPModeActive) {
            finishAndRemoveTask()
        }

        exoPlayer?.let { player ->
            videoPlayerPresenter.syncVideoTimestamp(player.currentPosition, player.duration)
        }

        videoPlayerPresenter.detachView(this)
        animator?.cancel()
        animator = null

        unbindService(videoServiceConnection)
        exoPlayer = null

        if (isFinishing) {
            stopService(VideoPlayerForegroundService.createBindingIntent(this))
        }

        super.onStop()
    }

    override fun setState(state: VideoPlayerView.State) {
        viewStateDelegate.switchState(state)

        when (state) {
            VideoPlayerView.State.Idle -> {
                animator?.cancel()
                animator = null
            }

            is VideoPlayerView.State.AutoplayPending -> {
                if (animator == null) {
                    animator = ValueAnimator.ofInt(state.progress, AUTOPLAY_PROGRESS_MAX)
                    animator
                        ?.apply {
                            addUpdateListener {
                                videoPlayerPresenter.onAutoplayProgressChanged(it.animatedValue as Int)
                            }
                            addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationCancel(animation: Animator) {
                                    animation.removeAllListeners()
                                }

                                override fun onAnimationEnd(animation: Animator) {
                                    videoPlayerPresenter.onAutoplayNext()
                                }
                            })
                            duration = ((1f - state.progress.toFloat() / AUTOPLAY_PROGRESS_MAX) * AUTOPLAY_ANIMATION_DURATION_MS).toLong()
                            start()
                        }
                }
                autoplayProgress.progress = state.progress

                // without if switch will stuck in one position
                if (!autoplaySwitch.isChecked) {
                    autoplaySwitch.isChecked = true
                }
            }

            VideoPlayerView.State.AutoplayCancelled -> {
                animator?.cancel()
                animator = null

                autoplayProgress.progress = AUTOPLAY_PROGRESS_MAX
                if (autoplaySwitch.isChecked) {
                    autoplaySwitch.isChecked = false
                }
            }

            VideoPlayerView.State.AutoplayNext ->
                move(StepNavigationDirection.NEXT)
        }
    }

    override fun setVideoPlayerData(videoPlayerData: VideoPlayerData) {
        Util.startForegroundService(this, VideoPlayerForegroundService.createIntent(this, videoPlayerData))

        videoRateChooser?.setImageDrawable(videoPlayerData.videoPlaybackRate.icon)

        qualityView.isVisible = true
        qualityView.text = getString(R.string.video_player_quality_icon, videoPlayerData.videoQuality)
        qualityView.setOnClickListener {
            val cachedVideo: Video? = videoPlayerData.mediaData.cachedVideo
            val externalVideo: Video? = videoPlayerData.mediaData.externalVideo
            val nowPlaying = videoPlayerData.videoUrl

            val dialog = VideoQualityDialogInPlayer.newInstance(externalVideo, cachedVideo, nowPlaying)
            if (!dialog.isAdded) {
                dialog.show(supportFragmentManager, null)
            }
        }
    }

    private fun showChooseRateMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.video_rate_menu)
        popupMenu.setOnMenuItemClickListener {
            videoPlayerPresenter
                .changePlaybackRate(VideoPlaybackRate.getValueById(it.itemId))
            true
        }
        popupMenu.setOnDismissListener {
            playerView.hideController()
        }
        popupMenu.show()
        playerView.showController()
    }

    override fun setIsLandscapeVideo(isLandScapeVideo: Boolean) {
        this.isLandscapeVideo = isLandScapeVideo

        @DrawableRes
        val fullScreenIconRes =
            if (isLandScapeVideo) {
                R.drawable.ic_fullscreen_exit
            } else {
                R.drawable.ic_fullscreen
            }

        exo_fullscreen_icon.setImageResource(fullScreenIconRes)
    }

    override fun showPlayInBackgroundPopup() {
        playerInBackroundPopup = PopupHelper
            .showPopupAnchoredToView(
                context    = this,
                anchorView = playerView,
                popupText  = getString(R.string.video_player_in_background_popup),
                theme      = PopupHelper.PopupTheme.LIGHT,
                cancelableOnTouchOutside = true,
                gravity    = Gravity.CENTER,
                withArrow  = false
            )
        playerView.postDelayed({ playerInBackroundPopup?.dismiss() }, IN_BACKGROUND_POPUP_TIMEOUT_MS)
    }

    override fun onQualityChanged(newUrlQuality: VideoUrl?) {
        videoPlayerPresenter.changeQuality(newUrlQuality)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        videoPlayerPresenter.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (!hasFocus &&
            !isFinishing &&
            exoPlayer?.playWhenReady == true) {
            analytic.reportAmplitudeEvent(AmplitudeAnalytic.Video.PLAY_IN_BACKGROUND)
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPIPModeActive = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            registerReceiver(pipReceiver, IntentFilter(ACTION_MEDIA_CONTROL))
        } else {
            unregisterReceiver(pipReceiver)
            if (exoPlayer?.isPlaying == false) {
                playerView.showController()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        resolveControlMargins()
    }

    override fun onBackPressed() {
        if (isLandscapeVideo) {
            changeVideoRotation()
        } else {
            super.onBackPressed()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun updatePictureInPictureActions(@DrawableRes iconId: Int, title: String, controlType: Int, requestCode: Int) {
        val actions = ArrayList<RemoteAction>()

        actions.add(
            RemoteAction(
                Icon.createWithResource(this, R.drawable.ic_replay_10_24),
                labelRewind,
                labelRewind,
                PendingIntentCompat.getBroadcast(
                    this,
                    REQUEST_REWIND,
                    Intent(ACTION_MEDIA_CONTROL)
                        .putExtra(EXTRA_CONTROL_TYPE, CONTROL_TYPE_REWIND),
                    flags = 0
                )
            )
        )

        val intent = PendingIntentCompat.getBroadcast(
            context = this,
            requestCode,
            Intent(ACTION_MEDIA_CONTROL)
                .putExtra(EXTRA_CONTROL_TYPE, controlType),
            flags = 0
        )
        val icon = Icon.createWithResource(this, iconId)
        actions.add(RemoteAction(icon, title, title, intent))

        actions.add(
            RemoteAction(
                Icon.createWithResource(this, R.drawable.ic_forward_10_24),
                labelForward,
                labelForward,
                PendingIntentCompat.getBroadcast(
                    context = this,
                    REQUEST_FORWARD,
                    Intent(ACTION_MEDIA_CONTROL)
                        .putExtra(EXTRA_CONTROL_TYPE, CONTROL_TYPE_FORWARD),
                    flags = 0
                )
            )
        )

        val pictureInPictureParamsBuilder = PictureInPictureParams.Builder()
        pictureInPictureParamsBuilder.setActions(actions)
        setPictureInPictureParams(pictureInPictureParamsBuilder.build())
    }

    private fun enterPipMode() {
        if (isSupportPIP()) {
            playerView.hideController()
            val params = PictureInPictureParams.Builder()
            this.enterPictureInPictureMode(params.build())
        }
    }

    private fun changeVideoRotation() {
        isLandscapeVideo = !isLandscapeVideo
        requestedOrientation = if (isLandscapeVideo) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        videoPlayerPresenter.changeVideoRotation(isLandscapeVideo)
    }

    private fun isSupportPIP(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

    private fun move(stepNavigationDirection: StepNavigationDirection) {
        if (isPIPModeActive) return
        lessonMoveNextIntent?.let {
            it.putExtra(LessonActivity.EXTRA_MOVE_STEP_NAVIGATION_DIRECTION, stepNavigationDirection.ordinal)
            startActivity(it)
        }
        finish()
    }

    private fun resolveControlMargins() {
        val (rewindMargin, skipMargin) =
            resources.getDimensionPixelOffset(R.dimen.video_player_rewind_margin) to resources.getDimensionPixelOffset(R.dimen.video_player_skip_margin)

        skip_prev.layoutParams = (skip_prev.layoutParams as ViewGroup.MarginLayoutParams).apply {
            rightMargin = skipMargin
        }
        rewind.layoutParams = (rewind.layoutParams as ViewGroup.MarginLayoutParams).apply {
            rightMargin = rewindMargin
        }
        forward.layoutParams = (forward.layoutParams as ViewGroup.MarginLayoutParams).apply {
            leftMargin = rewindMargin
        }
        skip_next.layoutParams = (skip_next.layoutParams as ViewGroup.MarginLayoutParams).apply {
            leftMargin = skipMargin
        }
    }

    private fun logIsPlayingEvent(isPlaying: Boolean) {
        val event = if (isPlaying) {
            VideoPlayerControlClickedEvent(VideoPlayerControlClickedEvent.ACTION_PLAY)
        } else {
            VideoPlayerControlClickedEvent(VideoPlayerControlClickedEvent.ACTION_PAUSE)
        }
        analytic.report(event)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end)
    }
}