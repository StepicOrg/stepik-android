package org.stepik.android.domain.feedback.interactor

import io.reactivex.Single
import org.stepic.droid.configuration.Config
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.feedback.model.SupportEmailData
import org.stepik.android.domain.feedback.repository.FeedbackRepository
import java.io.File
import javax.inject.Inject

class FeedbackInteractor
@Inject
constructor(
    private val config: Config,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val feedbackRepository: FeedbackRepository
) {
    fun createUriData(subject: String, aboutSystem: String): Single<SupportEmailData> =
        createSupportEmailData(fileContents = formSystemInfo(aboutSystem))
                .map { file -> SupportEmailData(
                    mailTo = config.supportEmail,
                    subject = subject,
                    body = file
                ) }

    private fun createSupportEmailData(fileName: String = "aboutsystem.txt", fileContents: String): Single<File> =
        feedbackRepository.createSupportEmailData(fileName, fileContents)

    private fun formSystemInfo(aboutSystem: String): String {
        val profileData = sharedPreferenceHelper.profile.let { profile ->
            "${profile?.fullName}\n${config.baseUrl}/users/${profile?.id}"
        }
        return "$profileData\n$aboutSystem"
    }
}