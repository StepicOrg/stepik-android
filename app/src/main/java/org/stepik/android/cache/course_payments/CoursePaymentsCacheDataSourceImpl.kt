package org.stepik.android.cache.course_payments

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.course_payments.source.CoursePaymentsCacheDataSource
import org.stepik.android.domain.course_payments.model.CoursePayment
import javax.inject.Inject

class CoursePaymentsCacheDataSourceImpl
@Inject
constructor() : CoursePaymentsCacheDataSource {

    override fun saveCoursePayments(coursePayments: List<CoursePayment>): Completable {
        return Completable.complete()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCoursePaymentsByCourseIds(vararg courseIds: Long): Single<List<CoursePayment>> {
        return Single.just(emptyList())
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}