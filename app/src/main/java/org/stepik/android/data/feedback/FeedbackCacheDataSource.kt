package org.stepik.android.data.feedback

import io.reactivex.Single
import java.io.File

interface FeedbackCacheDataSource {
    fun createSupportEmailData(fileName: String, fileContents: String): Single<File>
}