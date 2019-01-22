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

class VideoPlayerMediaDescriptionAdapter(
    private val context: Context,
    private val thumbnail: String? = null,
    private val title: String,
    private val description: String? = null,
    private val intent: Intent
) : PlayerNotificationManager.MediaDescriptionAdapter {
    override fun createCurrentContentIntent(player: Player?): PendingIntent =
        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    override fun getCurrentContentText(player: Player?): String? =
        description

    override fun getCurrentContentTitle(player: Player?): String =
        title

    override fun getCurrentLargeIcon(player: Player?, callback: PlayerNotificationManager.BitmapCallback?): Bitmap? {
        Glide.with(context)
            .load(thumbnail)
            .asBitmap()
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                    callback?.onBitmap(resource)
                }
            })
        return null
    }
}