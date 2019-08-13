package org.stepik.android.domain.certificates.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Certificate

interface CertificatesRepository {
    fun getCertificates(userId: Long, primarySourceType: DataSourceType = DataSourceType.REMOTE): Single<List<Certificate>>
}