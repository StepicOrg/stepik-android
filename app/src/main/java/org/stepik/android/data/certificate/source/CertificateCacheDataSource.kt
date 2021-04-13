package org.stepik.android.data.certificate.source

import io.reactivex.Completable
import io.reactivex.Single
import ru.nobird.android.core.model.PagedList
import org.stepik.android.model.Certificate

interface CertificateCacheDataSource {
    fun getCertificates(userId: Long): Single<PagedList<Certificate>>
    fun saveCertificates(certificates: List<Certificate>): Completable
}