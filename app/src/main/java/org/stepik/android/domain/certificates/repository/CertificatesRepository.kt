package org.stepik.android.domain.certificates.repository

import io.reactivex.Single
import org.stepik.android.model.Certificate

interface CertificatesRepository {
    fun getCertificates(userId: Long): Single<List<Certificate>>
}