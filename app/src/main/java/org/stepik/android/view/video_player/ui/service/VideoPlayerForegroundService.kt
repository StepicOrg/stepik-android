package org.stepik.android.view.video_player.ui.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioManager
import android.net.Uri
import android.os.Binder
import android.os.IBinder
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
import org.stepik.android.model.Video
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import org.stepik.android.view.video_player.ui.activity.VideoPlayerActivity
import org.stepik.android.view.video_player.ui.receiver.HeadphonesReceiver
import org.stepik.android.view.video_player.ui.receiver.InternetConnectionReceiverCompat

class VideoPlayerForegroundService : Service() {
    companion object {
        private const val EXTRA_EXTERNAL_VIDEO = "external_video"
        private const val EXTRA_CACHED_VIDEO = "cached_video"

        private const val PLAYER_CHANNEL_ID = "playback"
        private const val PLAYER_NOTIFICATION_ID = 21313

        private const val MEDIA_SESSION_TAG = "stepik_video"
    }

    private var player: SimpleExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null

    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    private var intent: Intent? = null

    private val headphonesReceiver =
        HeadphonesReceiver { player?.playWhenReady = false }

    private val internetConnectionReceiverCompat =
        InternetConnectionReceiverCompat {
            val player = this.player ?: return@InternetConnectionReceiverCompat
            val intent = this.intent ?: return@InternetConnectionReceiverCompat

            if (player.playbackState == Player.STATE_IDLE) {
                player.prepare(getMediaSource(intent))
                player.playWhenReady = true
            }
        }

    override fun onCreate() {
        internetConnectionReceiverCompat.registerReceiver(this)
    }

    override fun onBind(intent: Intent?): IBinder? =
        VideoPlayerBinder(player)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.intent = intent
        if (player == null && intent != null) {
            createPlayer(intent)
        }
        return START_STICKY
    }

    private fun createPlayer(intent: Intent) {
        val videoSource = getMediaSource(intent)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .build()

        player = ExoPlayerFactory
            .newSimpleInstance(this)
            .apply {
                playWhenReady = true
                setAudioAttributes(audioAttributes, true)
                prepare(videoSource)
            }

        val activityIntent = Intent(this, VideoPlayerActivity::class.java)
            .putExtras(intent)

        playerNotificationManager = PlayerNotificationManager
            .createWithNotificationChannel(
                this,
                PLAYER_CHANNEL_ID,
                R.string.video_player_control_notification_channel_name,
                PLAYER_NOTIFICATION_ID,
                object : PlayerNotificationManager.MediaDescriptionAdapter {
                    override fun createCurrentContentIntent(player: Player?): PendingIntent? =
                        PendingIntent.getActivity(this@VideoPlayerForegroundService, 333, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    override fun getCurrentContentText(player: Player?): String =
                        "Some string"

                    override fun getCurrentContentTitle(player: Player?): String =
                        "Some title"

                    override fun getCurrentLargeIcon(player: Player?, callback: PlayerNotificationManager.BitmapCallback?): Bitmap? =
                        null
                }
            )

        playerNotificationManager?.setNotificationListener(object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationCancelled(notificationId: Int) {
                stopSelf()
            }

            override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
                startForeground(notificationId, notification)
            }
        })

        playerNotificationManager?.setSmallIcon(R.drawable.ic_player_play)
        playerNotificationManager?.setStopAction(null)
        playerNotificationManager?.setPlayer(player)

        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG)
            .apply {
                isActive = true

                playerNotificationManager?.setMediaSessionToken(sessionToken)
            }
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector?.setPlayer(player, null)

        registerReceiver(headphonesReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
    }

    private fun getMediaSource(intent: Intent): MediaSource {
        val externalVideo: Video? = intent.getParcelableExtra(EXTRA_EXTERNAL_VIDEO)
        val cachedVideo: Video? = intent.getParcelableExtra(EXTRA_CACHED_VIDEO)

        val video = cachedVideo
            ?: externalVideo
            ?: throw IllegalArgumentException("No video specified")

        val bandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter)

        return ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(video.urls.minBy { it.quality?.toInt() ?: Integer.MAX_VALUE }!!.url))
    }

    private fun releasePlayer() {
        unregisterReceiver(headphonesReceiver)

        mediaSession?.release()
        mediaSessionConnector?.setPlayer(null,  null)

        playerNotificationManager?.setPlayer(null)
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