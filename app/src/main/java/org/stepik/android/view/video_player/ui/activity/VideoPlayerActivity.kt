package org.stepik.android.view.video_player.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video_player.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.video_player.VideoPlayerPresenter
import org.stepik.android.view.video_player.model.VideoPlayerData
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import org.stepik.android.view.video_player.ui.service.VideoPlayerForegroundService
import javax.inject.Inject

class VideoPlayerActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_VIDEO_PLAYER_DATA = "video_player_media_data"

        fun createIntent(context: Context, videoPlayerMediaData: VideoPlayerMediaData): Intent =
            Intent(context, VideoPlayerActivity::class.java)
                .putExtra(EXTRA_VIDEO_PLAYER_DATA, videoPlayerMediaData)
    }

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
                error?.printStackTrace()
            }
        }

    private var exoPlayer: ExoPlayer? = null
        set(value) {
            field?.removeListener(exoPlayerListener)
            field = value
            field?.addListener(exoPlayerListener)

            playerView?.player = value
        }

    private lateinit var videoPlayerPresenter: VideoPlayerPresenter

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val serviceIntent by lazy {
        val videoPlayerMediaData = intent.getParcelableExtra<VideoPlayerMediaData>(EXTRA_VIDEO_PLAYER_DATA)
        val videoId = videoPlayerMediaData.cachedVideo?.id ?: videoPlayerMediaData.externalVideo?.id ?: -1L
        val videoUrl = videoPlayerMediaData.cachedVideo?.urls?.firstOrNull()?.url
            ?: videoPlayerMediaData.externalVideo?.urls?.firstOrNull()?.url
            ?: ""
        val videoPlayerData = VideoPlayerData(videoId, videoUrl, 0, videoPlayerMediaData)
        VideoPlayerForegroundService.createIntent(this, videoPlayerData)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        videoPlayerPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(VideoPlayerPresenter::class.java)

        setTitle(R.string.video_title)
        setContentView(R.layout.activity_video_player)
        Util.startForegroundService(this, serviceIntent)
    }

    private fun injectComponent() {
        App.component()
            .videoPlayerComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        bindService(serviceIntent, videoServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        unbindService(videoServiceConnection)
        exoPlayer = null

        if (isFinishing) {
            stopService(serviceIntent)
        }

        super.onStop()
    }
}