package org.stepic.droid.ui.util

import android.graphics.Bitmap
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.widget.ImageView
import com.bumptech.glide.request.target.BitmapImageViewTarget

class RoundedBitmapImageViewTarget(
    private val borderRadius: Float,
    private val imageView: ImageView
): BitmapImageViewTarget(imageView) {
    override fun setResource(resource: Bitmap) {
        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(imageView.resources, resource)
        circularBitmapDrawable.cornerRadius = borderRadius
        imageView.setImageDrawable(circularBitmapDrawable)
    }
}