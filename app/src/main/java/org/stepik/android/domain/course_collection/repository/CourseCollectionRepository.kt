package org.stepik.android.domain.course_collection.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_collection.model.CourseCollectionQuery
import org.stepik.android.model.CourseCollection
import ru.nobird.android.domain.rx.first

interface CourseCollectionRepository {
    fun getCourseCollections(id: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Single<CourseCollection> =
        getCourseCollections(listOf(id), primarySourceType).first()

    fun getCourseCollections(ids: List<Long>, primarySourceType: DataSourceType = DataSourceType.CACHE): Single<List<CourseCollection>>
    fun getCourseCollections(query: CourseCollectionQuery, primarySourceType: DataSourceType = DataSourceType.REMOTE): Single<List<CourseCollection>>
}