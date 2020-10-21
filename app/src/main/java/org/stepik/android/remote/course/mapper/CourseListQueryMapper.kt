package org.stepik.android.remote.course.mapper

import org.stepik.android.domain.course_list.model.CourseListQuery
import ru.nobird.android.core.model.mapOfNotNull
import javax.inject.Inject

class CourseListQueryMapper
@Inject
constructor() {
    companion object {
        private const val PAGE = "page"
        private const val ORDER = "order"
        private const val TEACHER = "teacher"
        private const val LANGUAGE = "language"
        private const val IS_PUBLIC = "is_public"
        private const val IS_EXCLUDE_ENDED = "exclude_ended"
        private const val IS_CATALOGED = "is_cataloged"
        private const val IS_PAID = "is_paid"
        private const val WITH_CERTIFICATE = "with_certificate"
    }

    fun mapToQueryMap(courseListQuery: CourseListQuery): Map<String, String> =
        mapOfNotNull(
            PAGE to courseListQuery.page?.toString(),
            ORDER to courseListQuery.order?.order,
            TEACHER to courseListQuery.teacher?.toString(),
            LANGUAGE to courseListQuery.filterQuery?.language,
            IS_PUBLIC to courseListQuery.isPublic?.toString(),
            IS_EXCLUDE_ENDED to courseListQuery.isExcludeEnded?.toString(),
            IS_CATALOGED to courseListQuery.isCataloged?.toString(),
            IS_PAID to courseListQuery.filterQuery?.isPaid?.toString(),
            WITH_CERTIFICATE to courseListQuery.filterQuery?.withCertificate?.toString()
        )
}