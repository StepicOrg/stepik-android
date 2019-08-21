package org.stepik.android.data.certificates.source

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.model.Certificate

interface CertificatesRemoteDataSource {
    fun getCertificates(userId: Long, page: Int = 1): Single<PagedList<Certificate>>
}