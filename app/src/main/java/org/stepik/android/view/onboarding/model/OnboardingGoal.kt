package org.stepik.android.view.onboarding.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OnboardingGoal(
    @SerializedName("title")
    val title: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("course_lists")
    val courseLists: List<OnboardingCourseList>
) : Parcelable
