package org.stepik.android.data.course_collection.repository

import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepik.android.data.course_collection.source.CourseCollectionCacheDataSource
import org.stepik.android.data.course_collection.source.CourseCollectionRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_collection.repository.CourseCollectionRepository
import org.stepik.android.model.CourseCollection
import javax.inject.Inject

class CourseCollectionRepositoryImpl
@Inject
constructor(
    private val courseCollectionRemoteDataSource: CourseCollectionRemoteDataSource,
    private val courseCollectionCacheDataSource: CourseCollectionCacheDataSource
) : CourseCollectionRepository {
    override fun getCourseCollection(lang: String, primarySourceType: DataSourceType): Single<List<CourseCollection>> {
        val remoteSource = courseCollectionRemoteDataSource
            .getCourseCollectionList(lang)
            .doCompletableOnSuccess { courseCollectionCacheDataSource.replaceCourseCollectionList(lang, it) }

        val cacheSource = courseCollectionCacheDataSource
            .getCourseCollectionList(lang)

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource
                    .onErrorResumeNext(cacheSource)

            DataSourceType.CACHE ->
                cacheSource
                    .filter(List<CourseCollection>::isNotEmpty)
                    .switchIfEmpty(remoteSource)

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }
    }
}