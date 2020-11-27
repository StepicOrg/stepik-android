package org.stepik.android.domain.course_collection.interactor

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_collection.repository.CourseCollectionRepository
import org.stepik.android.model.CourseCollection
import javax.inject.Inject

class CourseCollectionInteractor
@Inject
constructor(
    private val courseCollectionRepository: CourseCollectionRepository
) {
    fun getCourseCollection(id: Long, dataSource: DataSourceType): Single<CourseCollection> =
        courseCollectionRepository
            .getCourseCollections(id, dataSource)
}