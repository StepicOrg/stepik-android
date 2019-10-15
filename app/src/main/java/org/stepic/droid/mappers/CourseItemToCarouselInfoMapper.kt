package org.stepic.droid.mappers

import org.stepic.droid.model.CoursesCarouselColorType
import org.stepic.droid.model.CoursesCarouselInfo
import org.stepik.android.model.CourseCollection
import javax.inject.Inject

class CourseItemToCarouselInfoMapper
@Inject
constructor() : Mapper<CourseCollection, CoursesCarouselInfo> {

    override fun map(item: CourseCollection): CoursesCarouselInfo {
        return CoursesCarouselInfo(
                colorType = CoursesCarouselColorType.Light,
                title = item.title,
                courseListType = null,
                courseIds = item.courses,
                description = item.description
        )
    }
}
