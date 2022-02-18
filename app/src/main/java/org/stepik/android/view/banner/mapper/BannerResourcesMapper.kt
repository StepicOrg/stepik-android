package org.stepik.android.view.banner.mapper

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import org.stepic.droid.R
import org.stepik.android.domain.banner.model.Banner
import javax.inject.Inject

class BannerResourcesMapper
@Inject
constructor() {
    @DrawableRes
    fun mapBannerTypeToImageResource(bannerType: Banner.ColorType?): Int =
        when (bannerType) {
            Banner.ColorType.BLUE ->
                R.drawable.ic_banner_blue
            Banner.ColorType.GREEN ->
                R.drawable.ic_banner_green
            Banner.ColorType.VIOLET ->
                R.drawable.ic_banner_violet
            else ->
                -1
        }

    @ColorRes
    fun mapBannerTypeToBackgroundColor(bannerType: Banner.ColorType?): Int =
        when (bannerType) {
            Banner.ColorType.BLUE ->
                R.color.color_blue_200
            Banner.ColorType.GREEN ->
                R.color.color_green_400_alpha_12
            Banner.ColorType.VIOLET ->
                R.color.color_violet_200
            else ->
                -1
        }

    @ColorInt
    fun mapBannerTypeToDescriptionTextColor(context: Context, bannerType: Banner.ColorType?): Int =
        when (bannerType) {
            Banner.ColorType.BLUE, Banner.ColorType.VIOLET ->
                ContextCompat.getColor(context, R.color.white)
            Banner.ColorType.GREEN ->
                ContextCompat.getColor(context, R.color.color_primary)
            else ->
                -1
        }
}