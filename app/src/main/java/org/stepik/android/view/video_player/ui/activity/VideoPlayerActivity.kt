package org.stepik.android.view.video_player.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video_player.*
import org.stepic.droid.R
import org.stepik.android.model.Video
import org.stepik.android.view.video_player.ui.service.VideoPlayerForegroundService

class VideoPlayerActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_EXTERNAL_VIDEO = "external_video"
        private const val EXTRA_CACHED_VIDEO = "cached_video"


        fun createIntent(context: Context, externalVideo: Video?, cachedVideo: Video?): Intent =
            Intent(context, VideoPlayerActivity::class.java)
                .putExtra(EXTRA_EXTERNAL_VIDEO, externalVideo)
                .putExtra(EXTRA_CACHED_VIDEO, cachedVideo)
    }

    private var player: SimpleExoPlayer? = null

    private val sharedPrefs by lazy { getSharedPreferences("test", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
    }

    override fun onStart() {
        super.onStart()

        Log.d("VideoPlayerActivity", "onStart")

        stopService(Intent(this, VideoPlayerForegroundService::class.java))

        val pos = sharedPrefs.getLong("pos", 0)

        val externalVideo: Video? = intent.getParcelableExtra(EXTRA_EXTERNAL_VIDEO)
        val cachedVideo: Video? = intent.getParcelableExtra(EXTRA_CACHED_VIDEO)

        val video = cachedVideo ?: externalVideo ?: return

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
                playerView.player = this

                playWhenReady = true
                setAudioAttributes(audioAttributes, true)
                prepare(videoSource)
                seekTo(pos)
            }
    }

    override fun onStop() {
        sharedPrefs.edit().putLong("pos", player?.currentPosition ?: 0).apply()

        playerView.player = null
        player?.release()
        player = null

        if (!isFinishing && !isChangingConfigurations) {
            val serviceIntent = Intent(this, VideoPlayerForegroundService::class.java)
                .putExtras(intent)
            Util.startForegroundService(this, serviceIntent)
        }

        Log.d("VideoPlayerActivity", "onStop; isFinishing = $isFinishing, isChangingConfigurations = $isChangingConfigurations")

        super.onStop()
    }
}