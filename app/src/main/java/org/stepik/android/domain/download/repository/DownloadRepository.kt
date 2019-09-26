package org.stepik.android.domain.download.repository

import io.reactivex.Single

interface DownloadRepository {
    fun getDownloadedCoursesIds(): Single<List<Long>>
}