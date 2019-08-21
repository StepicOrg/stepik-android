package org.stepik.android.data.certificates

import io.reactivex.Single
import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.certificates.repository.CertificatesRepository
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.model.Certificate
import javax.inject.Inject

class CertificatesInteractor
@Inject
constructor(
    private val certificatesRepository: CertificatesRepository,
    private val courseRepository: CourseRepository
) {

    fun getCertificates(userId: Long, page: Int = 1, sourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<CertificateViewItem>> {
        var courseIdToCertificateMap: Map<Long, Certificate> = mutableMapOf()
        return certificatesRepository.getCertificates(userId, page, sourceType)
            .flatMap { certificateList ->
                courseIdToCertificateMap = certificateList
                    .associateBy { it.course }
                Single.just(certificateList.map { it.course }.toLongArray())
            }
            .flatMap { courseIds ->
                courseRepository.getCourses(*courseIds).map { courses ->
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
}