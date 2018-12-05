package org.stepic.droid.storage.dao

import android.content.ContentValues
import org.stepic.droid.model.CourseListType
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.operations.ResultHandler
import org.stepic.droid.storage.structure.DbStructureCourse
import org.stepic.droid.storage.structure.DbStructureCourseList
import org.stepic.droid.util.getLong
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseListDaoImpl
@Inject
constructor(
    private val databaseOperations: DatabaseOperations,
    private val courseDao: IDao<Course>
) : CourseListDao {
    override fun addCourseList(courseListType: CourseListType, courses: List<Course>) {
        courseDao.insertOrReplaceAll(courses)

        val contentValues =
            courses.map {
                ContentValues(2).apply {
                    put(DbStructureCourseList.Columns.COURSE_ID, it.id)
                    put(DbStructureCourseList.Columns.TYPE, courseListType.name)
                }
            }
        databaseOperations.executeReplaceAll(DbStructureCourseList.TABLE_NAME, contentValues)
    }

    override fun getCourseList(courseListType: CourseListType): List<Course> {
        val ids =
            databaseOperations.executeQuery<String>(
                "SELECT ${DbStructureCourseList.Columns.COURSE_ID} " +
                        "FROM ${DbStructureCourseList.TABLE_NAME} " +
                        "WHERE ${DbStructureCourseList.Columns.TYPE} = ?",
                arrayOf(courseListType.name) ,
                ResultHandler { cursor ->
                    if (cursor.count <= 0) {
                        ""
                    } else {
                        val objects = ArrayList<Long>(cursor.count)
                        cursor.moveToFirst()
                        while (!cursor.isAfterLast) {
                            objects.add(cursor.getLong(DbStructureCourseList.Columns.COURSE_ID))
                        }
                        objects.joinToString(",")
                    }
                }
            )

        return courseDao.getAllInRange(DbStructureCourse.Columns.ID, ids)
    }

    override fun removeCourseList(courseListType: CourseListType) {
        databaseOperations.executeDelete(DbStructureCourseList.TABLE_NAME,
            DbStructureCourseList.Columns.TYPE + " = ?", arrayOf(courseListType.name))
    }

    override fun removeCourseFromList(courseListType: CourseListType, courseId: Long) {
        databaseOperations.executeDelete(DbStructureCourseList.TABLE_NAME,
            "${DbStructureCourseList.Columns.TYPE} = ? AND ${DbStructureCourseList.Columns.COURSE_ID} = ", arrayOf(courseListType.name, courseId.toString()))
    }

    override fun removeAll() {
        databaseOperations.executeDelete(DbStructureCourseList.TABLE_NAME, null, null)
    }
}