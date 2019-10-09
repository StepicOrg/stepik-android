package org.stepik.android.domain.personal_deadlines.model

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import androidx.annotation.StringRes
import org.stepic.droid.R
import org.stepic.droid.util.AppConstants

enum class LearningRate(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val millisPerWeek: Long
) : Parcelable {
    HOBBY(
            R.string.deadlines_learning_rate_hobby,
            R.drawable.ic_deadlines_learning_rate_hobby,
            AppConstants.MILLIS_IN_1HOUR * 3
    ),
    STANDARD(
            R.string.deadlines_learning_rate_standard,
            R.drawable.ic_deadlines_learning_rate_standard,
            AppConstants.MILLIS_IN_1HOUR * 7
    ),
    EXTREME(
            R.string.deadlines_learning_rate_extreme,
            R.drawable.ic_deadlines_learning_rate_extreme,
            AppConstants.MILLIS_IN_1HOUR * 15
    );

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LearningRate> {
        override fun createFromParcel(parcel: Parcel): LearningRate =
            LearningRate.values()[parcel.readInt()]

        override fun newArray(size: Int): Array<LearningRate?> =
            arrayOfNulls(size)
    }
}