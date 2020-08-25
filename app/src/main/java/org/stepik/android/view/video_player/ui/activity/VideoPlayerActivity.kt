package org.stepik.android.view.video_player.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.exo_playback_control_view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.preferences.VideoPlaybackRate
import org.stepic.droid.ui.custom_exo.NavigationBarUtil
import org.stepic.droid.ui.dialogs.VideoQualityDialogInPlayer
import org.stepic.droid.ui.util.PopupHelper
import org.stepik.android.model.Video
import org.stepik.android.model.VideoUrl
import org.stepik.android.presentation.video_player.VideoPlayerPresenter
import org.stepik.android.presentation.video_player.VideoPlayerView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import org.stepik.android.view.video_player.model.VideoPlayerData
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import org.stepik.android.view.video_player.ui.service.VideoPlayerForegroundService
import timber.log.Timber
import javax.inject.Inject

class VideoPlayerActivity : AppCompatActivity(), VideoPlayerView, VideoQualityDialogInPlayer.Callback {
    companion object {
        const val REQUEST_CODE = 1535

        private const val TIMEOUT_BEFORE_HIDE = 4000
        private const val JUMP_TIME_MILLIS = 10000

        private const val IN_BACKGROUND_POPUP_TIMEOUT_MS = 3000L

        private const val AUTOPLAY_PROGRESS_MAX = 3600
        private const val AUTOPLAY_ANIMATION_DURATION_MS = 7200L

        private const val EXTRA_VIDEO_PLAYER_DATA = "video_player_media_data"
        private const val EXTRA_VIDEO_AUTOPLAY = "video_player_autoplay"

        fun createIntent(context: Context, videoPlayerMediaData: VideoPlayerMediaData, isAutoplayEnabled: Boolean = false): Intent =
            Intent(context, VideoPlayerActivity::class.java)
                .putExtra(EXTRA_VIDEO_PLAYER_DATA, videoPlayerMediaData)
                .putExtra(EXTRA_VIDEO_AUTOPLAY, isAutoplayEnabled)
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val isAutoplayEnabled: Boolean by lazy { intent.getBooleanExtra(EXTRA_VIDEO_AUTOPLAY, false) }

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
        object : Player.EventListener {
            override fun onPlayerError(error: ExoPlaybackException?) {
                error ?: return

                Timber.d("Video source error: $error")

                if (error.type == ExoPlaybackException.TYPE_SOURCE && error.cause is HttpDataSource.HttpDataSourceException) {
                    Toast
                        .makeText(this@VideoPlayerActivity, R.string.no_connection, Toast.LENGTH_LONG)
                        .show()

                    analytic.reportError(Analytic.Video.CONNECTION_ERROR, error)
                } else {
                    analytic.reportError(Analytic.Video.ERROR, error)
                }
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                videoPlayerPresenter.onPlayerStateChanged(playbackState, isAutoplayEnabled)
            }
        }

    private var exoPlayer: ExoPlayer? = null
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
    private var isPIPModeEnabled = false

    private lateinit var videoPlayerPresenter: VideoPlayerPresenter
    private lateinit var viewStateDelegate: ViewStateDelegate<VideoPlayerView.State>

    private var animator: ValueAnimator? = null

    private var playerInBackroundPopup: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        videoPlayerPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(VideoPlayerPresenter::class.java)

        savedInstanceState
            ?.let(videoPlayerPresenter::onRestoreInstanceState)

        intent
            ?.getParcelableExtra<VideoPlayerMediaData>(EXTRA_VIDEO_PLAYER_DATA)
            ?.let(videoPlayerPresenter::onMediaData)

        setTitle(R.string.video_title)
        setContentView(R.layout.activity_video_player)

        closeButton.setOnClickListener {
            finish()
        }

        videoRateChooser.setOnClickListener {
            showChooseRateMenu(it)
        }

        qualityView.isVisible = false

        playerView.controllerShowTimeoutMs = TIMEOUT_BEFORE_HIDE
        playerView.setFastForwardIncrementMs(JUMP_TIME_MILLIS)
        playerView.setRewindIncrementMs(JUMP_TIME_MILLIS)

        exo_pip_icon_container.isVisible = supportsPip()
        exo_pip_icon_container.setOnClickListener { enterPipMode() }
        exo_fullscreen_icon_container.setOnClickListener { changeVideoRotation() }

        playerView.setControllerVisibilityListener { visibility ->
            if (visibility == View.VISIBLE) {
                NavigationBarUtil.hideNavigationBar(false, this)
            } else if (visibility == View.GONE) {
                NavigationBarUtil.hideNavigationBar(true, this)
            }
        }

        autoplay_controller_panel.setOnClickListener { videoPlayerPresenter.onAutoplayNext() }
        autoplayProgress.max = AUTOPLAY_PROGRESS_MAX
        autoplayCancel.setOnClickListener { videoPlayerPresenter.stayOnThisStep() }
        autoplaySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (autoplaySwitch.isUserTriggered) {
                videoPlayerPresenter.setAutoplayEnabled(isChecked)
            }
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

    override fun onStart() {
        super.onStart()
        videoPlayerPresenter.attachView(this)
        bindService(VideoPlayerForegroundService.createBindingIntent(this), videoServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        if (supportsPip() && isInPictureInPictureMode) {
            finishAndRemoveTask()
        }
        playerInBackroundPopup?.dismiss()
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

            VideoPlayerView.State.AutoplayNext -> {
                setResult(Activity.RESULT_OK)
                finish()
            }
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
    }

    override fun onBackPressed() {
        if (isLandscapeVideo) {
            changeVideoRotation()
        } else {
            super.onBackPressed()
        }
    }

    // For N devices that support it, not "officially"
    @Suppress("DEPRECATION")
    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            playerView.hideController()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                this.enterPictureInPictureMode(params.build())
            } else {
                this.enterPictureInPictureMode()
            }
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

    private fun supportsPip(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
}