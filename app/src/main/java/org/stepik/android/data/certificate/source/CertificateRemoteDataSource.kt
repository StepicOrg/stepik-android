package org.stepik.android.data.certificate.source

import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.app.core.model.PagedList
import org.stepik.android.model.Certificate

interface CertificateRemoteDataSource {
    fun getCertificate(userId: Long, courseId: Long): Maybe<Certificate>
    fun getCertificates(userId: Long, page: Int = 1): Single<PagedList<Certificate>>
    fun saveCertificate(certificate: Certificate): Single<Certificate>
}