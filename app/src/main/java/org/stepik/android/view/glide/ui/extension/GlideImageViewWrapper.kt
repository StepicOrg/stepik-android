package org.stepik.android.view.glide.ui.extension

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.stepik.android.view.glide.model.GlideRequestFactory

class GlideImageViewWrapper(
    val imageView: ImageView
) {
    private val svgRequestBuilder =
        GlideRequestFactory
            .create(imageView.context, null)
            .diskCacheStrategy(DiskCacheStrategy.DATA)

    fun setImagePath(path: String, placeholder: Drawable? = null) {
        svgRequestBuilder
            .load(Uri.parse(path))
            .placeholder(placeholder)
            .into(imageView)
    }
}

fun ImageView.wrapWithGlide(): GlideImageViewWrapper =
    GlideImageViewWrapper(this)