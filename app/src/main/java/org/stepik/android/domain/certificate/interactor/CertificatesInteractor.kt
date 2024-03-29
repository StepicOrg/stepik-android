package org.stepik.android.domain.certificate.interactor

import io.reactivex.Single
import org.stepic.droid.model.CertificateListItem
import ru.nobird.app.core.model.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.certificate.repository.CertificateRepository
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.model.Certificate
import org.stepik.android.model.Course
import javax.inject.Inject

class CertificatesInteractor
@Inject
constructor(
    private val certificateRepository: CertificateRepository,
    private val courseRepository: CourseRepository
) {
    fun getCertificates(userId: Long, page: Int = 1, sourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<CertificateListItem.Data>> =
        certificateRepository.getCertificates(userId, page, sourceType)
            .flatMap { certificates ->
                val courseIds =
                    certificates.map(Certificate::course)

                courseRepository
                    .getCourses(courseIds)
                    .map { courses ->
                        val coursesMap =
                            courses.associateBy(Course::id)

                        PagedList(
                            certificates.map { certificate ->
                                CertificateListItem.Data(
                                    certificate,
                                    coursesMap[certificate.course]?.title,
                                    coursesMap[certificate.course]?.cover
                                )
                            },

                            page = certificates.page,
                            hasNext = certificates.hasNext,
                            hasPrev = certificates.hasPrev
                        )
                    }
            }

    fun saveCertificate(certificate: Certificate): Single<Certificate> =
        certificateRepository.saveCertificate(certificate)
}