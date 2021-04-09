package org.stepik.android.data.certificate.source

import io.reactivex.Single
import ru.nobird.android.core.model.PagedList
import org.stepik.android.model.Certificate

interface CertificateRemoteDataSource {
    fun getCertificates(userId: Long, page: Int = 1): Single<PagedList<Certificate>>
}