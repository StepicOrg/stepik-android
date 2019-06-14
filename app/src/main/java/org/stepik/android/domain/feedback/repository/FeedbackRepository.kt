package org.stepik.android.domain.feedback.repository

import io.reactivex.Single
import java.io.File

interface FeedbackRepository {
    fun createSystemInfoData(fileName: String, fileContents: String): Single<File>
}