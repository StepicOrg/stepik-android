package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.StringRes
import org.stepic.droid.R

enum class OnboardingType(
        @StringRes
        val title: Int,
        @StringRes
        val subtitle: Int,
        private val isLast: Boolean) : Parcelable {
    FIRST(R.string.onboarding_first_title,
            R.string.onboarding_first_subtitle,
            isLast = false),
    SECOND(R.string.onboarding_second_title,
            R.string.onboarding_second_subtitle,
            isLast = false),
    THIRD(R.string.onboarding_third_title,
            R.string.onboarding_third_subtitle,
            isLast = false),
    FOURTH(R.string.onboarding_fourth_title,
            R.string.onboarding_fourth_subtitle,
            isLast = true);

    @StringRes
    fun getActionText(): Int {
        return if (isLast) {
            R.string.onboarding_done_action
        } else {
            R.string.onboarding_next_action
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<OnboardingType> {
        override fun createFromParcel(parcel: Parcel): OnboardingType =
                OnboardingType.values()[parcel.readInt()]

        override fun newArray(size: Int): Array<OnboardingType?> = arrayOfNulls(size)
    }
}
