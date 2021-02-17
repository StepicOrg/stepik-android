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
import org.stepik.android.domain.course_collection.repository.CourseCollectionRepository
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.CourseCollection
import org.stepik.android.presentation.course_list.CourseListCollectionView
import org.stepik.android.presentation.course_list.CourseListView
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

    fun getCourseCollectionNew(id: Long, viewSource: CourseViewSource): Flowable<CourseListCollectionView.State> =
        Flowable
            .fromArray(SourceTypeComposition.CACHE, SourceTypeComposition.REMOTE)
            .concatMapSingle { sourceType ->
                getCourseCollection(id, sourceType.generalSourceType)
                    .flatMap { collection ->
                        if (collection.courses.isEmpty()) {
                            Single.just(
                                CourseListCollectionView.State.Data(
                                    collection,
                                    CourseListView.State.Empty,
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

    private fun getSimilarCourseLists(id: List<Long>, dataSource: DataSourceType): Single<List<CatalogCourseList>> =
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

    private fun getSimilarAuthorLists(id: List<Long>, dataSource: DataSourceType): Single<List<CatalogAuthor>> =
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

    private fun resolveCollectionLoading(collection: CourseCollection, sourceType: SourceTypeComposition, viewSource: CourseViewSource): Single<CourseListCollectionView.State.Data> =
        if (collection.similarCourseLists.isNotEmpty() || collection.similarAuthors.isNotEmpty()) {
            loadWithSimilar(collection, sourceType, viewSource)
        } else {
            val ids = collection.courses.take(PAGE_SIZE)
            courseListInteractor
                .getCourseListItems(ids, sourceTypeComposition = sourceType, courseViewSource = viewSource)
                .map { items ->
                    CourseListCollectionView.State.Data(
                        collection,
                        CourseListView.State.Content(
                            courseListDataItems = items,
                            courseListItems = items
                        ),
                        sourceType = sourceType.generalSourceType
                    )
                }
        }

    private fun loadWithSimilar(collection: CourseCollection, sourceType: SourceTypeComposition, viewSource: CourseViewSource): Single<CourseListCollectionView.State.Data> =
        zip(
            courseListInteractor.getCourseListItems(collection.courses, sourceTypeComposition = sourceType, courseViewSource = viewSource),
            if (collection.similarCourseLists.isNotEmpty()) getSimilarCourseLists(collection.similarCourseLists, dataSource = sourceType.generalSourceType) else Single.just(emptyList()),
            if (collection.similarAuthors.isNotEmpty()) getSimilarAuthorLists(collection.similarAuthors, dataSource = sourceType.generalSourceType) else Single.just(emptyList())
        ) { items, similarCourses, authors ->
            CourseListCollectionView.State.Data(
                collection,
                CourseListView.State.Content(
                    courseListDataItems = items,
                    courseListItems = items + formHorizontalLists(authors, similarCourses)
                ),
                sourceType.generalSourceType
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