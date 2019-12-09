package org.stepik.android.remote.social_profile.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.SocialProfile
import org.stepik.android.remote.base.model.MetaResponse

class SocialProfilesResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("social-profiles")
    val socialProfiles: List<SocialProfile>
) : MetaResponse