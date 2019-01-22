package org.stepik.android.view.video_player.ui.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import org.stepic.droid.R
import org.stepik.android.model.Video
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import org.stepik.android.view.video_player.ui.activity.VideoPlayerActivity

class VideoPlayerForegroundService : Service() {
    companion object {
        private const val EXTRA_EXTERNAL_VIDEO = "external_video"
        private const val EXTRA_CACHED_VIDEO = "cached_video"

        private const val PLAYER_CHANNEL_ID = "playback"
        private const val PLAYER_NOTIFICATION_ID = 21313
    }

    private var player: SimpleExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
//    private val mediaSession: MediaSessionCompat? = null
//    private val mediaSessionConnector: MediaSessionConnector? = null

    override fun onBind(intent: Intent?): IBinder? =
        VideoPlayerBinder()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (player != null) return START_STICKY

        Log.d("VideoPlayerForegroundService", "onStartCommand")

        val externalVideo: Video? = intent.getParcelableExtra(EXTRA_EXTERNAL_VIDEO)
        val cachedVideo: Video? = intent.getParcelableExtra(EXTRA_CACHED_VIDEO)

        val video = cachedVideo ?: externalVideo ?: return START_STICKY

        val bandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter)

        val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(video.urls.minBy { it.quality?.toInt() ?: Integer.MAX_VALUE }!!.url))

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
                R.string.video_player_quality,
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

        playerNotificationManager?.setStopAction(null)

        playerNotificationManager?.setPlayer(player)

        return START_STICKY
    }

    override fun onDestroy() {
        playerNotificationManager?.setPlayer(null)
        player?.release()
        player = null

        Log.d("VideoPlayerForegroundService", "onDestroy")

        super.onDestroy()
    }

    inner class VideoPlayerBinder : Binder() {
        fun getPlayer(): ExoPlayer? =
            player
    }
}