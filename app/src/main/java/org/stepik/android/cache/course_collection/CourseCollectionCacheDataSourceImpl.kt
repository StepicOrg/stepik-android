package org.stepik.android.cache.course_collection

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DbParseHelper
import org.stepik.android.cache.course_collection.structure.DbStructureCourseCollection
import org.stepik.android.data.course_collection.source.CourseCollectionCacheDataSource
import org.stepik.android.domain.course_collection.model.CourseCollectionQuery
import org.stepik.android.model.CourseCollection
import org.stepik.android.model.PlatformType
import ru.nobird.android.core.model.mapOfNotNull
import javax.inject.Inject

class CourseCollectionCacheDataSourceImpl
@Inject
constructor(
    private val courseCollectionDao: IDao<CourseCollection>
) : CourseCollectionCacheDataSource {
    override fun getCourseCollections(ids: List<Long>): Single<List<CourseCollection>> =
        Single
            .fromCallable  {
                val stringIds = DbParseHelper.parseLongListToString(ids, AppConstants.COMMA).orEmpty()
                courseCollectionDao.getAllInRange(DbStructureCourseCollection.Columns.ID, stringIds)
            }

    override fun saveCourseCollections(items: List<CourseCollection>): Completable =
        Completable.fromCallable {
            courseCollectionDao.insertOrReplaceAll(items)
        }

    override fun getCourseCollections(query: CourseCollectionQuery): Single<List<CourseCollection>> =
        Single.fromCallable {
            query.platform
                .flatMap { platform ->
                    courseCollectionDao.getAll(mapToArgs(query.language, platform))
                }
                .sortedBy { it.position }
        }

    override fun replaceCourseCollections(
        query: CourseCollectionQuery,
        items: List<CourseCollection>
    ): Completable =
        Completable.fromCallable {
            query.platform.forEach { platform ->
                courseCollectionDao.remove(mapToArgs(query.language, platform))
            }
            courseCollectionDao.insertOrReplaceAll(items)
        }

    private fun mapToArgs(language: String?, platformType: PlatformType): Map<String, String> =
        mapOfNotNull(
            DbStructureCourseCollection.Columns.LANGUAGE to language,
            DbStructureCourseCollection.Columns.PLATFORM to platformType.id.toString()
        )
}