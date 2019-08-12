package org.stepik.android.data.certificates.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.Certificate

interface CertificatesCacheDataSource {
    fun getCertificates(userId: Long): Single<List<Certificate>>
    fun saveCertificates(certificates: List<Certificate>): Completable
}