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
    fun createSupportEmailData(subject: String, aboutSystem: String): Single<SupportEmailData> =
        createSystemInfoData(fileContents = formSystemInfoData(aboutSystem))
                .map { file -> SupportEmailData(
                    mailTo = config.supportEmail,
                    subject = subject,
                    body = file
                ) }

    private fun createSystemInfoData(fileName: String = "aboutsystem.txt", fileContents: String): Single<File> =
        feedbackRepository.createSystemInfoData(fileName, fileContents)

    private fun formSystemInfoData(aboutSystem: String): String {
        val profileData = sharedPreferenceHelper.profile.let { profile ->
            "${profile?.fullName}\n${config.baseUrl}/users/${profile?.id}"
        }
        return "$profileData\n$aboutSystem"
    }
}