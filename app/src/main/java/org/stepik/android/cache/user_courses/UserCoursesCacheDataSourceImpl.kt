package org.stepik.android.cache.user_courses

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.user_courses.structure.DbStructureUserCourse
import org.stepik.android.data.user_courses.source.UserCoursesCacheDataSource
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.user_courses.model.UserCourse
import javax.inject.Inject

class UserCoursesCacheDataSourceImpl
@Inject
constructor(
    private val userCourseDao: IDao<UserCourse>
) : UserCoursesCacheDataSource {
    companion object {
        private const val TRUE = "1"
        private const val FALSE = "0"
    }
    override fun getUserCourses(userCourseQuery: UserCourseQuery): Single<List<UserCourse>> =
        Single.fromCallable {
            userCourseDao.getAll(mapToDbQuery(userCourseQuery))
        }

    override fun getUserCourse(courseId: Long): Single<UserCourse> =
        Single.fromCallable {
            userCourseDao.get(DbStructureUserCourse.Columns.COURSE, courseId.toString())
        }

    override fun saveUserCourses(userCourses: List<UserCourse>): Completable =
        Completable.fromAction {
            userCourseDao.insertOrReplaceAll(userCourses)
        }

    override fun removeUserCourse(courseId: Long): Completable =
        Completable.fromAction {
            userCourseDao.remove(DbStructureUserCourse.Columns.ID, courseId.toString())
        }

    private fun mapToDbQuery(userCourseQuery: UserCourseQuery): Map<String, String> {
        val mutableMap = hashMapOf<String, String>()

        userCourseQuery.isFavorite?.let {
            mutableMap[DbStructureUserCourse.Columns.IS_FAVORITE] = mapBooleanToString(it)
        }
        userCourseQuery.isArchived?.let {
            mutableMap[DbStructureUserCourse.Columns.IS_ARCHIVED] = mapBooleanToString(it)
        }

        return mutableMap
    }

    private fun mapBooleanToString(value: Boolean): String =
        if (value) {
            TRUE
        } else {
            FALSE
        }
}