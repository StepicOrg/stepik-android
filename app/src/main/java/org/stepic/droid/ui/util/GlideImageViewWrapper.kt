package org.stepic.droid.ui.util

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.svg.GlideSvgRequestFactory

class GlideImageViewWrapper(
        val imageView: ImageView
) {
    private val svgRequestBuilder by lazy {
        GlideSvgRequestFactory
                .create(imageView.context, null)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
    }

    fun setImagePath(path: String, placeholder: Drawable? = null) {
        if (path.endsWith(AppConstants.SVG_EXTENSION)) {
            svgRequestBuilder
                .load(Uri.parse(path))
                .placeholder(placeholder)
                .into(imageView)
        } else {
            Glide.with(imageView.context)
                .asBitmap()
                .load(path)
                .placeholder(placeholder)
                .into(imageView)
        }
    }
}

fun ImageView.wrapWithGlide() = GlideImageViewWrapper(this)