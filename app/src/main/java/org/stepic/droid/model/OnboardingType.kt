package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import org.stepic.droid.R

private object AnimationPaths {
    const val FIRST_ANIMATION_PATH = "allAnimations/firstAnimation.json"
    const val SECOND_ANIMATION_PATH = "allAnimations/secondAnimation.json"
    const val THIRD_ANIMATION_PATH = "allAnimations/thirdAnimation.json"
    const val FOURTH_ANIMATION_PATH = "allAnimations/fourthAnimation.json"
}

enum class OnboardingType(
        @StringRes
        val title: Int,
        @StringRes
        val subtitle: Int,
        private val isLast: Boolean,
        val assetPathToAnimation: String) : Parcelable {


    FIRST(R.string.onboarding_first_title,
            R.string.onboarding_first_subtitle,
            isLast = false,
            assetPathToAnimation = AnimationPaths.FIRST_ANIMATION_PATH),
    SECOND(R.string.onboarding_second_title,
            R.string.onboarding_second_subtitle,
            isLast = false,
            assetPathToAnimation = AnimationPaths.SECOND_ANIMATION_PATH),
    THIRD(R.string.onboarding_third_title,
            R.string.onboarding_third_subtitle,
            isLast = false,
            assetPathToAnimation = AnimationPaths.THIRD_ANIMATION_PATH),
    FOURTH(R.string.onboarding_fourth_title,
            R.string.onboarding_fourth_subtitle,
            isLast = true,
            assetPathToAnimation = AnimationPaths.FOURTH_ANIMATION_PATH);

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
