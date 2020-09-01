package org.stepik.android.cache.course_payments

import io.reactivex.Completable
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
            val queryParams = mutableMapOf(DbStructureCoursePayments.Columns.COURSE to courseId.toString())
            coursePaymentStatus?.ordinal?.let {
                queryParams[DbStructureCoursePayments.Columns.STATUS] = it.toString()
            }
            coursePaymentsDao.getAll(queryParams)
        }

    override fun saveCoursePayments(coursePayments: List<CoursePayment>): Completable =
        Completable.fromCallable {
            coursePaymentsDao.insertOrReplaceAll(coursePayments)
        }
}