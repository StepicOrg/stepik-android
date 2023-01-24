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
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.session.MediaButtonReceiver
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ForwardingPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepik.android.domain.video_player.analytic.VideoPlayerControlClickedEvent
import org.stepik.android.view.video_player.model.VideoPlayerData
import org.stepik.android.view.video_player.ui.adapter.VideoPlayerMediaDescriptionAdapter
import org.stepik.android.view.video_player.ui.receiver.HeadphonesReceiver
import org.stepik.android.view.video_player.ui.receiver.InternetConnectionReceiverCompat
import javax.inject.Inject

class VideoPlayerForegroundService : Service() {
    companion object {
        private const val EXTRA_VIDEO_PLAYER_DATA = "video_player_data"

        private const val PLAYER_CHANNEL_ID = "playback"
        private const val PLAYER_NOTIFICATION_ID = 21313

        private const val MEDIA_SESSION_TAG = "stepik_video"

        private const val BACK_BUFFER_DURATION_MS = 60 * 1000

        private const val JUMP_TIME_MILLIS = 10000L

        fun createIntent(context: Context, videoPlayerData: VideoPlayerData): Intent =
            Intent(context, VideoPlayerForegroundService::class.java)
                .putExtra(EXTRA_VIDEO_PLAYER_DATA, videoPlayerData)

        fun createBindingIntent(context: Context): Intent =
            Intent(context, VideoPlayerForegroundService::class.java)
    }

    @Inject
    internal lateinit var analytic: Analytic

    private var player: ForwardingPlayer? = null
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

    private val mediaButtonReceiver =
        MediaButtonReceiver()

    private fun injectComponent() {
        App.component().inject(this)
    }

    override fun onCreate() {
        internetConnectionReceiverCompat.registerReceiver(this)
        injectComponent()
        createPlayer()
    }

    override fun onBind(intent: Intent?): IBinder =
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

        val loadControl = DefaultLoadControl.Builder()
            .setBackBuffer(BACK_BUFFER_DURATION_MS, true)
            .build()

        val simplePlayer = SimpleExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .setTrackSelector(DefaultTrackSelector(this))
            .setSeekBackIncrementMs(JUMP_TIME_MILLIS)
            .setSeekForwardIncrementMs(JUMP_TIME_MILLIS)
            .build()
            .apply {
                playWhenReady = true
                setAudioAttributes(audioAttributes, true)
            }

        player = object : ForwardingPlayer(simplePlayer) {
            /**
             * Interacting with seekbar in notification or fullscreen
             */
            override fun seekTo(windowIndex: Int, positionMs: Long) {
                val currentPosition = player?.currentPosition ?: 0L
                val difference = currentPosition - positionMs
                val action =
                    if (difference > 0L) {
                        VideoPlayerControlClickedEvent.ACTION_SEEK_BACK
                    } else {
                        VideoPlayerControlClickedEvent.ACTION_SEEK_FORWARD
                    }
                analytic.report(VideoPlayerControlClickedEvent(action))
                super.seekTo(windowIndex, positionMs)
            }

            /**
             * Rewind/forward buttons in notification
             */
            override fun seekBack() {
                analytic.report(VideoPlayerControlClickedEvent((VideoPlayerControlClickedEvent.ACTION_REWIND)))
                super.seekBack()
            }

            override fun seekForward() {
                analytic.report(VideoPlayerControlClickedEvent((VideoPlayerControlClickedEvent.ACTION_FORWARD)))
                super.seekForward()
            }

            /**
             * Rewind/forward when pressing PIP or fullscreen buttons
             */
            override fun seekTo(positionMs: Long) {
                val currentPosition = player?.currentPosition ?: 0L
                val difference = currentPosition - positionMs
                val action =
                    if (difference > 0L) {
                        VideoPlayerControlClickedEvent.ACTION_REWIND
                    } else {
                        VideoPlayerControlClickedEvent.ACTION_FORWARD
                    }
                analytic.report(VideoPlayerControlClickedEvent(action))
                super.seekTo(positionMs)
            }
        }

        val notificationListener =
            object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    stopSelf()
                }

                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                    startForeground(notificationId, notification)
                }
            }

        playerNotificationManager = PlayerNotificationManager.Builder(this, PLAYER_NOTIFICATION_ID, PLAYER_CHANNEL_ID)
            .setChannelNameResourceId(R.string.video_player_control_notification_channel_name)
            .setChannelDescriptionResourceId(R.string.video_player_control_notification_channel_description)
            .setMediaDescriptionAdapter(videoPlayerMediaDescriptionAdapter)
            .setNotificationListener(notificationListener)
            .build()

        playerNotificationManager.setSmallIcon(R.drawable.ic_player_notification)
        playerNotificationManager.setPlayer(player)
        playerNotificationManager.setUsePreviousAction(false)

        mediaSession =
            MediaSessionCompat(this, MEDIA_SESSION_TAG, ComponentName(this, MediaButtonReceiver::class.java), null)
        mediaSession.isActive = true

        registerReceiver(mediaButtonReceiver, IntentFilter(Intent.ACTION_MEDIA_BUTTON))

        playerNotificationManager.setMediaSessionToken(mediaSession.sessionToken)

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(player)

        registerReceiver(headphonesReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
    }

    private fun setPlayerData(videoPlayerData: VideoPlayerData?) {
        val player = this.player
            ?.wrappedPlayer as? SimpleExoPlayer
            ?: return

        val position =
            if (this.videoPlayerData?.videoId != videoPlayerData?.videoId) {
                videoPlayerData?.videoTimestamp ?: 0
            } else {
                player.currentPosition
            }

        val playWhenReady =
            this.videoPlayerData?.videoId != videoPlayerData?.videoId ||
            player.playWhenReady

        if (videoPlayerData != null) {
            if (this.videoPlayerData?.videoUrl != videoPlayerData.videoUrl || player.playbackState == Player.STATE_IDLE) {
                val mediaSource = getMediaSource(videoPlayerData)
                player.prepare()
                player.setMediaSource(mediaSource)
            }

            player.playbackParameters = PlaybackParameters(videoPlayerData.videoPlaybackRate.rateFloat, 1f)
            player.seekTo(position)
            player.playWhenReady = playWhenReady
        }

        videoPlayerMediaDescriptionAdapter.videoPlayerMediaData = videoPlayerData?.mediaData
        playerNotificationManager.invalidate()

        this.videoPlayerData = videoPlayerData
    }

    private fun getMediaSource(videoPlayerData: VideoPlayerData): MediaSource {
        val bandwidthMeter = DefaultBandwidthMeter.Builder(this).build()
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter)

        return ProgressiveMediaSource
            .Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.parse(videoPlayerData.videoUrl)))
    }

    private fun releasePlayer() {
        unregisterReceiver(headphonesReceiver)
        unregisterReceiver(mediaButtonReceiver)

        mediaSession.release()
        mediaSessionConnector.setPlayer(null)

        playerNotificationManager.setPlayer(null)
        player?.release()
        player = null
    }

    override fun onDestroy() {
        releasePlayer()
        internetConnectionReceiverCompat.unregisterReceiver(this)
        super.onDestroy()
    }

    class VideoPlayerBinder(private val player: Player?) : Binder() {
        fun getPlayer(): Player? =
            player
    }
}