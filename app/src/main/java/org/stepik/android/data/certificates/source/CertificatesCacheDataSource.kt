package org.stepik.android.data.certificates.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.model.Certificate

interface CertificatesCacheDataSource {
    fun getCertificates(userId: Long): Single<PagedList<Certificate>>
    fun saveCertificates(certificates: List<Certificate>): Completable
}