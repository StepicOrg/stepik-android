package org.stepik.android.domain.course_collection.interactor

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.catalog.model.CatalogAuthor
import org.stepik.android.domain.catalog.model.CatalogCourseList
import org.stepik.android.domain.course_collection.repository.CourseCollectionRepository
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.CourseCollection
import javax.inject.Inject

class CourseCollectionInteractor
@Inject
constructor(
    private val courseCollectionRepository: CourseCollectionRepository,
    private val userRepository: UserRepository
) {
    fun getCourseCollection(id: Long, dataSource: DataSourceType): Single<CourseCollection> =
        courseCollectionRepository
            .getCourseCollections(id, dataSource)

    fun getSimilarCourseLists(id: List<Long>, dataSource: DataSourceType): Single<List<CatalogCourseList>> =
        courseCollectionRepository
            .getCourseCollections(id, dataSource)
            .map { it.map { courseCollection ->
                CatalogCourseList(
                    id = courseCollection.id,
                    title = courseCollection.title,
                    description = courseCollection.description,
                    courses = courseCollection.courses,
                    coursesCount = courseCollection.courses.size
                )
            } }

    fun getSimilarAuthorLists(id: List<Long>, dataSource: DataSourceType): Single<List<CatalogAuthor>> =
        userRepository
            .getUsers(id, dataSource)
            .map { it.map { user ->
                CatalogAuthor(
                    id = user.id,
                    isOrganization = user.isOrganization,
                    fullName = user.fullName ?: "",
                    alias = null,
                    avatar = user.avatar ?: "",
                    createdCoursesCount = user.createdCoursesCount.toInt(),
                    followersCount = user.followersCount.toInt()
                )
            } }
}