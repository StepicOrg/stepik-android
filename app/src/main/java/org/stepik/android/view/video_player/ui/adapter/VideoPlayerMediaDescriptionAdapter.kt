package org.stepik.android.view.video_player.ui.adapter

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import org.stepic.droid.R
import org.stepik.android.view.video_player.model.VideoPlayerData
import org.stepik.android.view.video_player.ui.activity.VideoPlayerActivity

class VideoPlayerMediaDescriptionAdapter(
    private val context: Context
) : PlayerNotificationManager.MediaDescriptionAdapter {
    var videoPlayerData: VideoPlayerData? = null

    private fun createIntent(videoPlayerData: VideoPlayerData): Intent =
        VideoPlayerActivity.createIntent(context, videoPlayerData)

    override fun createCurrentContentIntent(player: Player?): PendingIntent? =
        videoPlayerData
            ?.let(::createIntent)
            ?.let { intent ->
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

    override fun getCurrentContentText(player: Player?): String? =
        videoPlayerData?.description

    override fun getCurrentContentTitle(player: Player?): String =
        videoPlayerData?.title ?: ""

    override fun getCurrentLargeIcon(player: Player?, callback: PlayerNotificationManager.BitmapCallback?): Bitmap? {
        Glide.with(context)
            .load(videoPlayerData?.thumbnail)
            .asBitmap()
            .placeholder(R.drawable.general_placeholder)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                    callback?.onBitmap(resource)
                }
            })
        return null
    }
}