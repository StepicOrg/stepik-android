package org.stepik.android.data.certificate.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.core.model.PagedList
import org.stepik.android.model.Certificate

interface CertificateCacheDataSource {
    fun getCertificate(userId: Long, courseId: Long): Maybe<Certificate>
    fun getCertificates(userId: Long): Single<PagedList<Certificate>>

    fun saveCertificate(certificate: Certificate): Completable =
        saveCertificates(listOf(certificate))

    fun saveCertificates(certificates: List<Certificate>): Completable
}