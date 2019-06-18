package org.stepik.android.data.feedback.repository

import io.reactivex.Single
import org.stepik.android.data.feedback.source.FeedbackCacheDataSource
import org.stepik.android.domain.feedback.repository.FeedbackRepository
import java.io.File
import javax.inject.Inject

class FeedbackRepositoryImpl
@Inject
constructor(
    private val feedbackCacheDataSource: FeedbackCacheDataSource
) : FeedbackRepository {
    override fun createSystemInfoData(fileName: String, fileContents: String): Single<File> =
        feedbackCacheDataSource.createSystemInfoData(fileName, fileContents)
}