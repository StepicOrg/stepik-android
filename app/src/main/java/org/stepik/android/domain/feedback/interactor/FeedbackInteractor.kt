package org.stepik.android.domain.feedback.interactor

import org.stepic.droid.configuration.Config
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.feedback.model.SupportEmailData
import javax.inject.Inject

class FeedbackInteractor
@Inject
constructor(
    private val config: Config,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    fun createUriData(subject: String, aboutSystem: String): SupportEmailData =
        SupportEmailData(
            mailTo = config.supportEmail,
            subject = subject,
            body = formBodyField(aboutSystem)
        )

    private fun formBodyField(aboutSystem: String): String {
        val profileData = sharedPreferenceHelper.profile.let { profile ->
            "${profile?.fullName}\n${config.baseUrl}/users/${profile?.id}"
        }
        return "$profileData\n$aboutSystem"
    }
}