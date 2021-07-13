package org.stepik.android.remote.course_revenue

import io.reactivex.Single
import org.stepik.android.data.course_revenue.source.CourseBeneficiariesRemoteDataSource
import org.stepik.android.domain.course_revenue.model.CourseBeneficiary
import org.stepik.android.remote.course_revenue.model.CourseBeneficiariesResponse
import org.stepik.android.remote.course_revenue.service.CourseBeneficiariesService
import ru.nobird.android.domain.rx.first
import javax.inject.Inject

class CourseBeneficiariesRemoteDataSourceImpl
@Inject
constructor(
    private val courseBeneficiariesService: CourseBeneficiariesService
) : CourseBeneficiariesRemoteDataSource {
    override fun getCourseBeneficiary(courseId: Long): Single<CourseBeneficiary> =
        courseBeneficiariesService
            .getCourseBeneficiaries(courseId)
            .map(CourseBeneficiariesResponse::courseBeneficiaries)
            .first()
}