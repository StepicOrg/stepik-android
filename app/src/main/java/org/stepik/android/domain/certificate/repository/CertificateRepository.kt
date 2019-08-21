package org.stepik.android.domain.certificate.repository

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Certificate

interface CertificateRepository {
    fun getCertificates(userId: Long, page: Int = 1, sourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<Certificate>>
}