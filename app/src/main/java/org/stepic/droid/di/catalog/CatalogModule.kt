package org.stepic.droid.di.catalog

import dagger.Binds
import dagger.Module
import org.stepic.droid.mappers.CourseItemToCarouselInfoMapper
import org.stepic.droid.mappers.Mapper
import org.stepik.android.model.CourseCollection
import org.stepic.droid.model.CoursesCarouselInfo

@Module
interface CatalogModule {

    @Binds
    fun bindMapper(courseListToCarouselInfoMapper: CourseItemToCarouselInfoMapper): Mapper<CourseCollection, CoursesCarouselInfo>
}
