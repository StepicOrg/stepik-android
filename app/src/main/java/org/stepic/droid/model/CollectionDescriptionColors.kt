package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import org.stepic.droid.R

enum class CollectionDescriptionColors(
        @DrawableRes
        val backgroundRes: Int,
        @DrawableRes
        val backgroundResSquared: Int,
        @ColorRes
        val textColorRes: Int
): Parcelable {
    BLUE(R.drawable.gradient_background_blue, R.drawable.gradient_background_blue_squared, R.color.text_color_gradient_blue),
    FIRE(R.drawable.gradient_background_fire, R.drawable.gradient_background_fire_squared, R.color.text_color_gradient_fire);

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    companion object CREATOR : Parcelable.Creator<CollectionDescriptionColors> {
        override fun createFromParcel(parcel: Parcel): CollectionDescriptionColors
                = CollectionDescriptionColors.values()[parcel.readInt()]

        override fun newArray(size: Int): Array<CollectionDescriptionColors?>
                = arrayOfNulls(size)
    }
}
