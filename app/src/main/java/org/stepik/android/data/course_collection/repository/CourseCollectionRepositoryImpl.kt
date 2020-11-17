package org.stepik.android.data.course_collection.repository

import io.reactivex.Single
import org.stepik.android.data.base.repository.delegate.ListRepositoryDelegate
import org.stepik.android.data.course_collection.source.CourseCollectionCacheDataSource
import org.stepik.android.data.course_collection.source.CourseCollectionRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_collection.model.CourseCollectionQuery
import org.stepik.android.domain.course_collection.repository.CourseCollectionRepository
import org.stepik.android.model.CourseCollection
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class CourseCollectionRepositoryImpl
@Inject
constructor(
    private val courseCollectionRemoteDataSource: CourseCollectionRemoteDataSource,
    private val courseCollectionCacheDataSource: CourseCollectionCacheDataSource
) : CourseCollectionRepository {
    private val delegate =
        ListRepositoryDelegate(
            courseCollectionRemoteDataSource::getCourseCollections,
            courseCollectionCacheDataSource::getCourseCollections,
            courseCollectionCacheDataSource::saveCourseCollections
        )

    override fun getCourseCollections(ids: List<Long>, primarySourceType: DataSourceType): Single<List<CourseCollection>> =
        delegate.get(ids, primarySourceType, allowFallback = true)

    override fun getCourseCollections(query: CourseCollectionQuery, primarySourceType: DataSourceType): Single<List<CourseCollection>> {
        val remoteSource = courseCollectionRemoteDataSource
            .getCourseCollections(query)
            .doCompletableOnSuccess { courseCollectionCacheDataSource.replaceCourseCollections(query, it) }

        val cacheSource = courseCollectionCacheDataSource
            .getCourseCollections(query)

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