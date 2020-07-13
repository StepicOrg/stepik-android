package org.stepik.android.cache.course_list

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.course_list.structure.DbStructureCourseListQuery
import org.stepik.android.data.course_list.model.CourseListQueryData
import org.stepik.android.data.course_list.source.CourseListQueryCacheDataSource
import org.stepik.android.domain.course_list.model.CourseListQuery
import javax.inject.Inject

class CourseListQueryCacheDataSourceImpl
@Inject
constructor(
    private val courseListQueryDataDao: IDao<CourseListQueryData>
) : CourseListQueryCacheDataSource {
    override fun getCourses(courseListQuery: CourseListQuery): Single<LongArray> =
        Single.fromCallable {
            courseListQueryDataDao
                .get(DbStructureCourseListQuery.Columns.ID, courseListQuery.toString())
                ?.courses
        }

    override fun saveCourses(courseListQuery: CourseListQuery, courses: LongArray): Completable =
        Completable.fromCallable {
            courseListQueryDataDao.insertOrReplace(CourseListQueryData(courseListQueryId = courseListQuery.toString(), courses = courses))
        }
}