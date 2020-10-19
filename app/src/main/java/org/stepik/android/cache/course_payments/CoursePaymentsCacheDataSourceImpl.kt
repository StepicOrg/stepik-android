package org.stepik.android.cache.course_payments

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.course_payments.structure.DbStructureCoursePayments
import org.stepik.android.data.course_payments.source.CoursePaymentsCacheDataSource
import org.stepik.android.domain.course_payments.model.CoursePayment
import ru.nobird.android.core.model.mapOfNotNull
import javax.inject.Inject

class CoursePaymentsCacheDataSourceImpl
@Inject
constructor(
    private val coursePaymentsDao: IDao<CoursePayment>
) : CoursePaymentsCacheDataSource {

    override fun getCoursePaymentsByCourseId(courseId: Long, coursePaymentStatus: CoursePayment.Status?): Single<List<CoursePayment>> =
        Single.fromCallable {
            val queryParams =
                mapOfNotNull(
                    DbStructureCoursePayments.Columns.COURSE to courseId.toString(),
                    DbStructureCoursePayments.Columns.STATUS to coursePaymentStatus?.ordinal?.toString()
                )
            coursePaymentsDao.getAll(queryParams)
        }

    override fun saveCoursePayments(coursePayments: List<CoursePayment>): Completable =
        Completable.fromCallable {
            coursePaymentsDao.insertOrReplaceAll(coursePayments)
        }
}