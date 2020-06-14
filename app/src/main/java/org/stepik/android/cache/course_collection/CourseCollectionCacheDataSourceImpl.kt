package org.stepik.android.cache.course_collection

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.course_collection.structure.DbStructureCourseCollection
import org.stepik.android.data.course_collection.source.CourseCollectionCacheDataSource
import org.stepik.android.model.CourseCollection
import javax.inject.Inject

class CourseCollectionCacheDataSourceImpl
@Inject
constructor(
    private val courseCollectionDao: IDao<CourseCollection>
) : CourseCollectionCacheDataSource {
    override fun getCourseCollectionList(lang: String): Single<List<CourseCollection>> =
        Single.fromCallable {
            courseCollectionDao.getAll(DbStructureCourseCollection.Columns.LANGUAGE, lang)
        }

    override fun replaceCourseCollectionList(
        lang: String,
        items: List<CourseCollection>
    ): Completable =
        Completable.fromCallable {
            courseCollectionDao.remove(DbStructureCourseCollection.Columns.LANGUAGE, lang)
            courseCollectionDao.insertOrReplaceAll(items)
        }
}