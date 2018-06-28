package org.stepic.droid.ui.util

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
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
    }

    fun setImagePath(path: String) {
        if (path.endsWith(AppConstants.SVG_EXTENSION)) {
            svgRequestBuilder
                    .load(Uri.parse(path))
                    .into(imageView)
        } else {
            Glide.with(imageView.context)
                    .load(path)
                    .asBitmap()
                    .into(imageView)
        }
    }
}

fun ImageView.wrapWithGlide() = GlideImageViewWrapper(this)