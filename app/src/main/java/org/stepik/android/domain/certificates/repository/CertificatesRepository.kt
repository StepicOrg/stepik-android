package org.stepik.android.domain.certificates.repository

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Certificate

interface CertificatesRepository {
    fun getCertificates(userId: Long, page: Int = 1, sourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<Certificate>>
}