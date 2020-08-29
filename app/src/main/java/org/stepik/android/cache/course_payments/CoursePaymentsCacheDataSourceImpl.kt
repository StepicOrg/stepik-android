package org.stepik.android.cache.course_payments

import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.course_payments.structure.DbStructureCoursePayments
import org.stepik.android.data.course_payments.source.CoursePaymentsCacheDataSource
import org.stepik.android.domain.course_payments.model.CoursePayment
import javax.inject.Inject

class CoursePaymentsCacheDataSourceImpl
@Inject
constructor(
    private val coursePaymentsDao: IDao<CoursePayment>
) : CoursePaymentsCacheDataSource {

    override fun getCoursePaymentsByCourseId(courseId: Long, coursePaymentStatus: CoursePayment.Status?): Single<List<CoursePayment>> =
        Single.fromCallable {
            val payments = coursePaymentsDao
                .getAll(DbStructureCoursePayments.Columns.COURSE, courseId.toString())
            if (coursePaymentStatus != null) {
                payments.filter { it.status == coursePaymentStatus }
            } else {
                payments
            }
        }
}