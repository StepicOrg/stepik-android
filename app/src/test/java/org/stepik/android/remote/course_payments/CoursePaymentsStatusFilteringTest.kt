package org.stepik.android.remote.course_payments

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.model.Meta
import org.stepik.android.remote.course_payments.model.CoursePaymentsResponse
import org.stepik.android.remote.course_payments.service.CoursePaymentService

@RunWith(Parameterized::class)
class CoursePaymentsStatusFilteringTest(private val status: CoursePayment.Status) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}")
        fun statuses() = CoursePayment.Status.entries.map { arrayOf(it) }
    }

    @Test fun filtersByStatusPreservingEveryMatchingPayment() {
        val course = 4_000_000_001L
        val service = mock<CoursePaymentService>()
        val payments = CoursePayment.Status.entries.toTypedArray().flatMapIndexed { index, value ->
            listOf(
                CoursePayment(
                    id = index.toLong(),
                    course = course,
                    isPaid = false,
                    status = value,
                    user = 900
                ), CoursePayment(
                    id = 100 + index.toLong(),
                    course = course,
                    isPaid = true,
                    status = value,
                    user = 901
                )
            )
        }
        whenever(service.getCoursePaymentsByCourseId(course))
            .thenReturn(
                Single.just(
                    CoursePaymentsResponse(
                        meta = Meta(page = 1, hasNext = false, hasPrevious = false),
                        coursePayments = payments
                    )
                )
            )
        CoursePaymentsRemoteDataSourceImpl(service)
            .getCoursePaymentsByCourseId(course, status)
            .test()
            .assertResult(payments.filter { it.status == status })
        verify(service).getCoursePaymentsByCourseId(course)
    }
}
