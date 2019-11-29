package org.stepik.android.presentation.profile_detail

import org.stepik.android.domain.profile.model.ProfileData

interface ProfileDetailView {
    fun setState(profileData: ProfileData?)
}