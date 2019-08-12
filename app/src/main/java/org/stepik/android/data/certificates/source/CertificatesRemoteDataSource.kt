package org.stepik.android.data.certificates.source

import io.reactivex.Single
import org.stepik.android.model.Certificate

interface CertificatesRemoteDataSource {
    fun getCertificates(userId: Long): Single<List<Certificate>>
}