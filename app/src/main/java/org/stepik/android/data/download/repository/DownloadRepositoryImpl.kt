package org.stepik.android.data.download.repository

import io.reactivex.Single
import org.stepik.android.data.download.source.DownloadCacheDataSource
import org.stepik.android.domain.download.repository.DownloadRepository
import javax.inject.Inject

class DownloadRepositoryImpl
@Inject
constructor(
    private val downloadCacheDataSource: DownloadCacheDataSource
) : DownloadRepository {
    override fun getDownloadedCoursesIds(): Single<List<Long>> =
        downloadCacheDataSource.getDownloadedCoursesIds()
}