package org.stepic.droid.util.glide

import android.graphics.Picture
import android.graphics.RectF
import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder

class ImageResourceTranscoder : ResourceTranscoder<ImageResource, PictureDrawable> {
    override fun transcode(
        toTranscode: Resource<ImageResource>,
        options: Options
    ): Resource<PictureDrawable>? {
        val picture =
            when (val res = toTranscode.get()) {
                is ImageResource.Vector ->
                    res.svg.renderToPicture()

                is ImageResource.Raster ->
                    Picture()
                        .apply {
                            val canvas = beginRecording(res.bitmap.width, res.bitmap.height)
                            canvas.drawBitmap(res.bitmap, null, RectF(0f, 0f, res.bitmap.width.toFloat(), res.bitmap.height.toFloat()), null)
                            endRecording()
                        }
            }

        return SimpleResource(PictureDrawable(picture))
    }
}