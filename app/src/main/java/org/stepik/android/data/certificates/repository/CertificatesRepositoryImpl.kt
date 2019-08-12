package org.stepik.android.data.certificates.repository

import io.reactivex.Single
import org.stepik.android.data.certificates.source.CertificatesRemoteDataSource
import org.stepik.android.domain.certificates.repository.CertificatesRepository
import org.stepik.android.model.Certificate
import javax.inject.Inject

class CertificatesRepositoryImpl
@Inject
constructor(
    private val certificatesRemoteDataSource: CertificatesRemoteDataSource
) : CertificatesRepository {
    override fun getCertificates(userId: Long): Single<List<Certificate>> =
        certificatesRemoteDataSource.getCertificates(userId)
}