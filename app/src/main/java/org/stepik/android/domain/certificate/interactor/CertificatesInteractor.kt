package org.stepik.android.domain.certificate.interactor

import io.reactivex.Single
import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.util.PagedList
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
    fun getCertificates(userId: Long, page: Int = 1, sourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<CertificateViewItem>> =
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
                                CertificateViewItem(
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
}