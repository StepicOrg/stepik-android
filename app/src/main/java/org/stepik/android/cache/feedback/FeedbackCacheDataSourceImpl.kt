package org.stepik.android.cache.feedback

import android.content.Context
import io.reactivex.Single
import org.stepik.android.data.feedback.source.FeedbackCacheDataSource
import java.io.File
import javax.inject.Inject

class FeedbackCacheDataSourceImpl
@Inject
constructor(
    private val context: Context
) : FeedbackCacheDataSource {
    override fun createSystemInfoData(fileName: String, fileContents: String): Single<File> =
        Single.fromCallable {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(fileContents.toByteArray())
            }
            File(context.filesDir, fileName)
        }
}