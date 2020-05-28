package org.stepik.android.view.glide.model

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import org.stepik.android.view.glide.mapper.SvgSoftwareLayerSetter

object GlideRequestFactory {
    @JvmStatic
    fun create(
        context: Context,
        placeholder: Drawable?
    ): RequestBuilder<PictureDrawable> =
        Glide.with(context)
            .`as`(PictureDrawable::class.java)
            .placeholder(placeholder)
            .listener(SvgSoftwareLayerSetter())
}