package org.stepik.android.data.progress.repository

import io.reactivex.Single
import org.stepik.android.data.progress.source.ProgressRemoteDataSource
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.model.Progress
import javax.inject.Inject

class ProgressRepositoryImpl
@Inject
constructor(
    private val progressRemoteDataSource: ProgressRemoteDataSource
) : ProgressRepository {
    override fun getProgress(progressId: String): Single<Progress> =
            progressRemoteDataSource.getProgress(progressId)
}