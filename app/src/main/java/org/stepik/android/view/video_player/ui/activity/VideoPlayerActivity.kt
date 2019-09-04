package org.stepik.android.view.video_player.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.PopupMenu
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import android.widget.Toast
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.exo_playback_control_view.*
import kotlinx.android.synthetic.main.exo_playback_control_view.view.*
import kotlinx.android.synthetic.main.exo_simple_player_view.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.preferences.VideoPlaybackRate
import org.stepic.droid.ui.custom_exo.NavigationBarUtil
import org.stepic.droid.ui.dialogs.VideoQualityDialogInPlayer
import org.stepic.droid.ui.util.PopupHelper
import org.stepic.droid.ui.util.changeVisibility
import org.stepik.android.model.Video
import org.stepik.android.model.VideoUrl
import org.stepik.android.presentation.video_player.VideoPlayerPresenter
import org.stepik.android.presentation.video_player.VideoPlayerView
import org.stepik.android.view.video_player.model.VideoPlayerData
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import org.stepik.android.view.video_player.ui.service.VideoPlayerForegroundService
import javax.inject.Inject

class VideoPlayerActivity : AppCompatActivity(), VideoPlayerView, VideoQualityDialogInPlayer.Callback {
    companion object {
        private const val TIMEOUT_BEFORE_HIDE = 4000
        private const val JUMP_TIME_MILLIS = 10000

        private const val IN_BACKGROUND_POPUP_TIMEOUT_MS = 3000L

        private const val EXTRA_VIDEO_PLAYER_DATA = "video_player_media_data"

        fun createIntent(context: Context, videoPlayerMediaData: VideoPlayerMediaData): Intent =
            Intent(context, VideoPlayerActivity::class.java)
                .putExtra(EXTRA_VIDEO_PLAYER_DATA, videoPlayerMediaData)
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

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

                if (error.type == ExoPlaybackException.TYPE_SOURCE && error.cause is HttpDataSource.HttpDataSourceException) {
                    Toast
                        .makeText(this@VideoPlayerActivity, R.string.no_connection, Toast.LENGTH_LONG)
                        .show()

                    analytic.reportError(Analytic.Video.CONNECTION_ERROR, error)
                } else {
                    analytic.reportError(Analytic.Video.ERROR, error)
                }
            }
        }

    private var exoPlayer: ExoPlayer? = null
        set(value) {
            field?.removeListener(exoPlayerListener)
            field = value
            field?.addListener(exoPlayerListener)

            playerView?.player = value
        }
    private var isLandscapeVideo = false

    private lateinit var videoPlayerPresenter: VideoPlayerPresenter

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

        moreItemsView.changeVisibility(false)

        playerView.controllerShowTimeoutMs = TIMEOUT_BEFORE_HIDE
        playerView.setFastForwardIncrementMs(JUMP_TIME_MILLIS)
        playerView.setRewindIncrementMs(JUMP_TIME_MILLIS)

        playerView.exo_controller.exo_fullscreen_icon.setOnClickListener { changeVideoRotation() }

        playerView.setControllerVisibilityListener { visibility ->
            if (visibility == View.VISIBLE) {
                NavigationBarUtil.hideNavigationBar(false, this)
            } else if (visibility == View.GONE) {
                NavigationBarUtil.hideNavigationBar(true, this)
            }
        }
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
        playerInBackroundPopup?.dismiss()
        exoPlayer?.let { player ->
            videoPlayerPresenter.syncVideoTimestamp(player.currentPosition, player.duration)
        }

        videoPlayerPresenter.detachView(this)

        unbindService(videoServiceConnection)
        exoPlayer = null

        if (isFinishing) {
            stopService(VideoPlayerForegroundService.createBindingIntent(this))
        }

        super.onStop()
    }

    override fun setVideoPlayerData(videoPlayerData: VideoPlayerData) {
        Util.startForegroundService(this, VideoPlayerForegroundService.createIntent(this, videoPlayerData))

        videoRateChooser?.setImageDrawable(videoPlayerData.videoPlaybackRate.icon)

        moreItemsView.changeVisibility(true)
        moreItemsView.setOnClickListener {
            showMoreItemsPopup(it, videoPlayerData)
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

    private fun showMoreItemsPopup(view: View, videoPlayerData: VideoPlayerData) {
        val morePopupMenu = PopupMenu(this, view)
        morePopupMenu.inflate(R.menu.video_more_menu)

        morePopupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.video_quality -> {
                    val cachedVideo: Video? = videoPlayerData.mediaData.cachedVideo
                    val externalVideo: Video? = videoPlayerData.mediaData.externalVideo
                    val nowPlaying = videoPlayerData.videoUrl

                    val dialog = VideoQualityDialogInPlayer.newInstance(externalVideo, cachedVideo, nowPlaying)
                    if (!dialog.isAdded) {
                        dialog.show(supportFragmentManager, null)
                    }

                    true
                }
                else -> false
            }
        }

        morePopupMenu.setOnDismissListener {
            playerView.hideController()
        }
        morePopupMenu.show()
        playerView.showController()
    }

    override fun setIsLandscapeVideo(isLandScapeVideo: Boolean) {
        this.isLandscapeVideo = isLandScapeVideo
        if (isLandscapeVideo) {
            playerView.exo_controller.exo_fullscreen_icon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_fullscreen_exit))
        } else {
            playerView.exo_controller.exo_fullscreen_icon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_fullscreen))
        }
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

    override fun onBackPressed() {
        if (isLandscapeVideo) {
            changeVideoRotation()
        } else {
            super.onBackPressed()
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
}