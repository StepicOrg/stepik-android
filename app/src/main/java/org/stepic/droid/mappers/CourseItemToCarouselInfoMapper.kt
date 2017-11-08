package org.stepic.droid.mappers

import org.stepic.droid.model.CourseListItem
import org.stepic.droid.model.CoursesCarouselColorType
import org.stepic.droid.model.CoursesCarouselInfo
import javax.inject.Inject

class CourseItemToCarouselInfoMapper
@Inject
constructor() : Mapper<CourseListItem, CoursesCarouselInfo> {

    override fun map(item: CourseListItem): CoursesCarouselInfo {
        return CoursesCarouselInfo(
                colorType = CoursesCarouselColorType.Light,
                title = item.title,
                table = null,
                courseIds = item.courses,
                description = item.description
        )
    }
}
