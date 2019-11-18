package org.stepik.android.cache.download

import io.reactivex.Single
import org.stepik.android.cache.download.dao.DownloadedCoursesDao
import org.stepik.android.data.download.source.DownloadCacheDataSource
import javax.inject.Inject

class DownloadCacheDataSourceImpl
@Inject
constructor(
    private val downloadedCoursesDao: DownloadedCoursesDao
) : DownloadCacheDataSource {
    override fun getDownloadedCoursesIds(): Single<List<Long>> =
        Single.fromCallable {
            downloadedCoursesDao.courseIds
        }
}