package org.stepik.android.cache.certificates.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepic.droid.util.PagedList
import org.stepik.android.cache.certificates.structure.DbStructureCertificate
import org.stepik.android.data.certificates.source.CertificatesCacheDataSource
import org.stepik.android.model.Certificate
import javax.inject.Inject

class CertificatesCacheDataSourceImpl
@Inject
constructor(
    private val certificateDao: IDao<Certificate>
) : CertificatesCacheDataSource {
    override fun getCertificates(userId: Long): Single<PagedList<Certificate>> =
        Single.fromCallable {
            PagedList(certificateDao.getAll(DbStructureCertificate.Columns.USER, userId.toString()))
        }

    override fun saveCertificates(certificates: List<Certificate>): Completable =
        Completable.fromAction {
            certificateDao.insertOrReplaceAll(certificates)
        }
}