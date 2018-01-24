package org.stepic.droid.adaptive.util

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.DbParseHelper
import javax.inject.Inject

class AdaptiveCoursesResolver
@Inject
constructor(
        private val firebaseRemoteConfig: FirebaseRemoteConfig,
        private val userPreferences: UserPreferences
) {

    fun isAdaptive(courseId: Long) =
            userPreferences.isAdaptiveModeEnabled &&
            DbParseHelper.parseStringToLongArray(firebaseRemoteConfig.getString(RemoteConfig.ADAPTIVE_COURSES),",")
                    ?.contains(courseId)
                    ?: false

}