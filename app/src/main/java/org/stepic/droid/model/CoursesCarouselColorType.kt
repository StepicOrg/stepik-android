package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import org.stepic.droid.R

enum class CoursesCarouselColorType(
        @ColorRes
        val textColor: Int,
        @DrawableRes
        val continueResource: Int,
        @DrawableRes
        val joinResource: Int,
        @ColorRes
        val backgroundColorRes: Int,
        @ColorRes
        val viewAllColorRes: Int
) : Parcelable {

    Light(R.color.new_accent_color,
            R.drawable.course_widget_continue_background,
            R.drawable.course_widget_join_background,
            R.color.transparent,
            R.color.view_all_course_list_color
    ),
    Dark(R.color.white,
            R.drawable.course_widget_continue_dark_background,
            R.drawable.course_widget_join_background,
            R.color.new_accent_color,
            R.color.view_all_course_list_color_dark
    );

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(ordinal)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CoursesCarouselColorType> = object : Parcelable.Creator<CoursesCarouselColorType> {
            override fun createFromParcel(source: Parcel): CoursesCarouselColorType =
                    CoursesCarouselColorType.values()[source.readInt()]

            override fun newArray(size: Int): Array<CoursesCarouselColorType?> = arrayOfNulls(size)
        }
    }
}
