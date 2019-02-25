package org.stepik.android.domain.settings.interactor

import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.preferences.UserPreferences
import javax.inject.Inject

class VideoQualityInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val userPreferences: UserPreferences
) {
    fun getVideoQuality(): String? =
        userPreferences
            .qualityVideo
            .takeUnless { sharedPreferenceHelper.isNeedToShowVideoQualityExplanation }
}