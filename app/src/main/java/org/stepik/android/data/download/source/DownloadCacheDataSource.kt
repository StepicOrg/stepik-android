package org.stepik.android.data.download.source

import io.reactivex.Single

interface DownloadCacheDataSource {
    fun getDownloadedCoursesIds(): Single<List<Long>>
}