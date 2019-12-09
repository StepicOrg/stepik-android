package org.stepik.android.presentation.profile_id

import org.stepik.android.domain.profile.model.ProfileData

interface ProfileIdView {
    fun setState(profileData: ProfileData?)
}