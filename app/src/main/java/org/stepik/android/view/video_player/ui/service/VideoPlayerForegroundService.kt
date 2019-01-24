package org.stepik.android.view.video_player.ui.service

import android.app.Notification
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import org.stepic.droid.R
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import org.stepik.android.view.video_player.model.VideoPlayerData
import org.stepik.android.view.video_player.ui.adapter.VideoPlayerMediaDescriptionAdapter
import org.stepik.android.view.video_player.ui.receiver.HeadphonesReceiver
import org.stepik.android.view.video_player.ui.receiver.InternetConnectionReceiverCompat

class VideoPlayerForegroundService : Service() {
    companion object {
        private const val EXTRA_VIDEO_PLAYER_DATA = "video_player_data"

        private const val PLAYER_CHANNEL_ID = "playback"
        private const val PLAYER_NOTIFICATION_ID = 21313

        private const val MEDIA_SESSION_TAG = "stepik_video"

        fun createIntent(context: Context, videoPlayerData: VideoPlayerData): Intent =
            Intent(context, VideoPlayerForegroundService::class.java)
                .putExtra(EXTRA_VIDEO_PLAYER_DATA, videoPlayerData)
    }

    private var player: SimpleExoPlayer? = null
    private lateinit var playerNotificationManager: PlayerNotificationManager

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var videoPlayerMediaDescriptionAdapter: VideoPlayerMediaDescriptionAdapter

    private var videoPlayerData: VideoPlayerData? = null

    private val headphonesReceiver =
        HeadphonesReceiver { player?.playWhenReady = false }

    private val internetConnectionReceiverCompat =
        InternetConnectionReceiverCompat {
            val player = this.player ?: return@InternetConnectionReceiverCompat
            val videoPlayerData = this.videoPlayerData ?: return@InternetConnectionReceiverCompat

            if (player.playbackState == Player.STATE_IDLE) {
                setPlayerData(videoPlayerData)
            }
        }

    override fun onCreate() {
        internetConnectionReceiverCompat.registerReceiver(this)
        createPlayer()
    }

    override fun onBind(intent: Intent?): IBinder? =
        VideoPlayerBinder(player)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setPlayerData(intent?.getParcelableExtra(EXTRA_VIDEO_PLAYER_DATA))
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
    }

    private fun createPlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .build()

        videoPlayerMediaDescriptionAdapter = VideoPlayerMediaDescriptionAdapter(this)

        player = ExoPlayerFactory
            .newSimpleInstance(this)
            .apply {
                playWhenReady = true
                setAudioAttributes(audioAttributes, true)
            }

        playerNotificationManager = PlayerNotificationManager
            .createWithNotificationChannel(
                this,
                PLAYER_CHANNEL_ID,
                R.string.video_player_control_notification_channel_name,
                PLAYER_NOTIFICATION_ID,
                videoPlayerMediaDescriptionAdapter
            )

        playerNotificationManager.setNotificationListener(object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationCancelled(notificationId: Int) {
                stopSelf()
            }

            override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
                startForeground(notificationId, notification)
            }
        })

        playerNotificationManager.setSmallIcon(R.drawable.ic_player_notification)
        playerNotificationManager.setStopAction(null)
        playerNotificationManager.setPlayer(player)

        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG, ComponentName(this, MediaButtonReceiver::class.java), null)
        mediaSession.isActive = true

        playerNotificationManager.setMediaSessionToken(mediaSession.sessionToken)

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(player, null)

        registerReceiver(headphonesReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
    }

    private fun setPlayerData(videoPlayerData: VideoPlayerData?) {
        val player = this.player
            ?: return

        val position =
            if (this.videoPlayerData?.videoId != videoPlayerData?.videoId) {
                videoPlayerData?.startPosition ?: 0
            } else {
                player.currentPosition
            }

        if (videoPlayerData != null) {
            val mediaSource = getMediaSource(videoPlayerData)
            player.prepare(mediaSource)
            player.seekTo(position)
            player.playWhenReady = true
        }

        videoPlayerMediaDescriptionAdapter.videoPlayerMediaData = videoPlayerData?.mediaData
        playerNotificationManager.invalidate()

        this.videoPlayerData = videoPlayerData
    }

    private fun getMediaSource(videoPlayerData: VideoPlayerData): MediaSource {
        val bandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter)

        return ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(videoPlayerData.videoUrl))
    }

    private fun releasePlayer() {
        unregisterReceiver(headphonesReceiver)

        mediaSession.release()
        mediaSessionConnector.setPlayer(null,  null)

        playerNotificationManager.setPlayer(null)
        player?.release()
        player = null
    }

    override fun onDestroy() {
        releasePlayer()
        internetConnectionReceiverCompat.unregisterReceiver(this)
        super.onDestroy()
    }

    class VideoPlayerBinder(private val player: ExoPlayer?) : Binder() {
        fun getPlayer(): ExoPlayer? =
            player
    }
}