package org.stepik.android.domain.course_collection.interactor

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.catalog.model.CatalogAuthor
import org.stepik.android.domain.catalog.model.CatalogCourseList
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course.model.SourceTypeComposition
import org.stepik.android.domain.course_collection.model.CourseCollectionResult
import org.stepik.android.domain.course_collection.repository.CourseCollectionRepository
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.CourseCollection
import javax.inject.Inject

class CourseCollectionInteractor
@Inject
constructor(
    private val courseListInteractor: CourseListInteractor,
    private val courseCollectionRepository: CourseCollectionRepository,
    private val userRepository: UserRepository
) {
    companion object {
        private const val PAGE_SIZE = 20
    }

    fun getCourseCollectionNew(id: Long, viewSource: CourseViewSource): Flowable<CourseCollectionResult> =
        Flowable
            .fromArray(SourceTypeComposition.CACHE, SourceTypeComposition.REMOTE)
            .concatMapSingle { sourceType ->
                getCourseCollection(id, sourceType.generalSourceType)
                    .flatMap { collection ->
                        if (collection.courses.isEmpty()) {
                            Single.just(
                                CourseCollectionResult(
                                    courseCollection = collection,
                                    courseListDataItems = PagedList(emptyList()),
                                    courseListItems = emptyList(),
                                    sourceType = sourceType.generalSourceType
                                )
                            )
                        } else {
                            resolveCollectionLoading(collection, sourceType, viewSource)
                        }
                    }
            }

    fun getCourseListItems(
        courseId: List<Long>,
        courseViewSource: CourseViewSource,
        sourceTypeComposition: SourceTypeComposition = SourceTypeComposition.REMOTE
    ): Single<PagedList<CourseListItem.Data>> =
        courseListInteractor.getCourseListItems(courseId, courseViewSource, sourceTypeComposition)

    private fun getCourseCollection(id: Long, dataSource: DataSourceType): Single<CourseCollection> =
        courseCollectionRepository
            .getCourseCollections(id, dataSource)

    private fun getSimilarCourseLists(ids: List<Long>, dataSource: DataSourceType): Single<List<CatalogCourseList>> =
        courseCollectionRepository
            .getCourseCollections(ids, dataSource)
            .map { it.map { courseCollection ->
                CatalogCourseList(
                    id = courseCollection.id,
                    title = courseCollection.title,
                    description = courseCollection.description,
                    courses = courseCollection.courses,
                    coursesCount = courseCollection.courses.size
                )
            } }

    private fun getSimilarAuthorLists(ids: List<Long>, dataSource: DataSourceType): Single<List<CatalogAuthor>> =
        userRepository
            .getUsers(ids, dataSource)
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

    private fun resolveCollectionLoading(collection: CourseCollection, sourceType: SourceTypeComposition, viewSource: CourseViewSource): Single<CourseCollectionResult> =
        zip(
            courseListInteractor.getCourseListItems(
                courseIds =
                    if (collection.similarCourseLists.isNotEmpty() || collection.similarAuthors.isNotEmpty()) {
                        collection.courses
                    } else {
                        collection.courses.take(PAGE_SIZE)
                    },
                sourceTypeComposition = sourceType,
                courseViewSource = viewSource
            ),
            getSimilarCourseLists(collection.similarCourseLists, dataSource = sourceType.generalSourceType),
            getSimilarAuthorLists(collection.similarAuthors, dataSource = sourceType.generalSourceType)
        ) { items, similarCourses, authors ->
            CourseCollectionResult(
                courseCollection = collection,
                courseListDataItems = items,
                courseListItems = items + formHorizontalLists(authors, similarCourses),
                sourceType = sourceType.generalSourceType
            )
        }

    private fun formHorizontalLists(authors: List<CatalogAuthor>, similarCourses: List<CatalogCourseList>): List<CourseListItem> {
        val list = mutableListOf<CourseListItem>()
        if (authors.isNotEmpty()) {
            list += CourseListItem.SimilarAuthors(authors)
        }
        if (similarCourses.isNotEmpty()) {
            list += CourseListItem.SimilarCourses(similarCourses)
        }
        return list
    }
}