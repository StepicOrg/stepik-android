package org.stepik.android.domain.certificate.repository

import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.app.core.model.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Certificate

interface CertificateRepository {
    fun getCertificate(userId: Long, courseId: Long, sourceType: DataSourceType = DataSourceType.CACHE): Maybe<Certificate>
    fun getCertificates(userId: Long, page: Int = 1, sourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<Certificate>>
    fun saveCertificate(certificate: Certificate): Single<Certificate>
}