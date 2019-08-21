package org.stepik.android.domain.certificates.interactor

import io.reactivex.Single
import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.certificates.repository.CertificatesRepository
import org.stepik.android.domain.course.repository.CourseRepository
import javax.inject.Inject

class CertificatesInteractor
@Inject
constructor(
    private val certificatesRepository: CertificatesRepository,
    private val courseRepository: CourseRepository
) {
    fun getCertificates(userId: Long, page: Int = 1, sourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<CertificateViewItem>> =
        certificatesRepository.getCertificates(userId, page, sourceType)
            .flatMap { Single.just(it) }
            .flatMap { certificateList ->
                courseRepository.getCourses(*certificateList.map { it.course }.toLongArray()).map { courses ->
                    val courseIdToCertificateMap = certificateList
                        .associateBy { it.course }
                    PagedList(
                        courses.map {
                            val certificateRelatedToCourse = courseIdToCertificateMap[it.id]
                            CertificateViewItem(
                                certificateRelatedToCourse!!,
                                it.title,
                                it.cover
                            )
                        }
                    )
                }
            }
}