package org.stepik.android.data.certificates

import io.reactivex.Single
import org.stepic.droid.configuration.Config
import org.stepic.droid.model.CertificateViewItem
import org.stepik.android.domain.certificates.repository.CertificatesRepository
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.model.Certificate
import javax.inject.Inject

class CertificatesInteractor
@Inject
constructor(
    private val config: Config,
    private val certificatesRepository: CertificatesRepository,
    private val courseRepository: CourseRepository
) {

    fun getCertificates(userId: Long): Single<List<CertificateViewItem>> {
        var courseIdToCertificateMap: Map<Long, Certificate> = mutableMapOf()
        return certificatesRepository.getCertificates(userId)
            .flatMap { certificateList ->
                courseIdToCertificateMap = certificateList
                    .filterNot { it.course == null }
                    .associateBy { it.course!! }
                Single.just(certificateList.mapNotNull { it.course }.toLongArray())
            }
            .flatMap { courseIds ->
                courseRepository.getCourses(*courseIds).map { courses ->
                    courses.map {
                        val certificateRelatedToCourse = courseIdToCertificateMap[it.id]
                        var cover: String? = null
                        if (it.cover != null) {
                            cover = config.baseUrl + it.cover
                        }
                        CertificateViewItem(
                            certificateRelatedToCourse?.id,
                            it.title,
                            cover,
                            certificateRelatedToCourse?.type,
                            certificateRelatedToCourse?.url,
                            certificateRelatedToCourse?.grade,
                            certificateRelatedToCourse?.issueDate
                        )
                    }
                }
            }
    }
}