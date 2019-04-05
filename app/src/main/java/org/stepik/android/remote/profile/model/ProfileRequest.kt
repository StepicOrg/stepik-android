package org.stepik.android.remote.profile.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.user.Profile

class ProfileRequest(
    @SerializedName("profile")
    val profile: Profile
)