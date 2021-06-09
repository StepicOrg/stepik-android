package org.stepik.android.cache.certificates.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import ru.nobird.android.core.model.PagedList
import org.stepik.android.cache.certificates.structure.DbStructureCertificate
import org.stepik.android.data.certificate.source.CertificateCacheDataSource
import org.stepik.android.model.Certificate
import javax.inject.Inject

class CertificateCacheDataSourceImpl
@Inject
constructor(
    private val certificateDao: IDao<Certificate>
) : CertificateCacheDataSource {
    override fun getCertificate(userId: Long, courseId: Long): Maybe<Certificate> =
        Maybe
            .fromCallable {
                certificateDao
                    .get(mapOf(
                        DbStructureCertificate.Columns.USER to userId.toString(),
                        DbStructureCertificate.Columns.COURSE to courseId.toString()
                    ))
            }

    override fun getCertificates(userId: Long): Single<PagedList<Certificate>> =
        Single.fromCallable {
            PagedList(certificateDao.getAll(DbStructureCertificate.Columns.USER, userId.toString()))
        }

    override fun saveCertificates(certificates: List<Certificate>): Completable =
        Completable.fromAction {
            certificateDao.insertOrReplaceAll(certificates)
        }
}