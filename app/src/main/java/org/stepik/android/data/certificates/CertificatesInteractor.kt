package org.stepik.android.data.certificates

import io.reactivex.Single
import org.stepic.droid.model.CertificateViewItem
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

    fun getCertificates(userId: Long): Single<List<CertificateViewItem>> {
        var courseIdToCertificateMap: Map<Long, Certificate> = mutableMapOf()
        return certificatesRepository.getCertificates(userId)
            .flatMap { certificateList ->
                courseIdToCertificateMap = certificateList
                    .associateBy { it.course }
                Single.just(certificateList.map { it.course }.toLongArray())
            }
            .flatMap { courseIds ->
                courseRepository.getCourses(*courseIds).map { courses ->
                    courses.map {
                        val certificateRelatedToCourse = courseIdToCertificateMap[it.id]
                        CertificateViewItem(
                            certificateRelatedToCourse!!,
                            it.title,
                            it.cover
                        )
                    }
                }
            }
    }
}