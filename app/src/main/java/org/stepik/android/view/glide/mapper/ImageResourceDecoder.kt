package org.stepik.android.view.glide.mapper

import android.graphics.BitmapFactory
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import org.stepik.android.view.glide.model.ImageResource
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class ImageResourceDecoder : ResourceDecoder<InputStream, ImageResource> {
    override fun decode(
        source: InputStream,
        width: Int,
        height: Int,
        options: Options
    ): Resource<ImageResource>? {
        val bytes = source.readBytes()

        return try {
            val svg = SVG.getFromInputStream(ByteArrayInputStream(bytes))
            SimpleResource(ImageResource.Vector(svg))
        } catch (ex: SVGParseException) {
            try {
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                SimpleResource(ImageResource.Raster(bitmap))
            } catch (e: Exception) {
                throw IOException("Can't load image resource", e)
            }
        }
    }

    override fun handles(source: InputStream, options: Options): Boolean =
        true
}