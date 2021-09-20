package org.stepik.android.domain.course_search.interactor

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import org.stepic.droid.util.then
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_search.model.CourseSearchResult
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.search.repository.SearchRepository
import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.domain.search_result.repository.SearchResultRepository
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.Lesson
import org.stepik.android.model.Progress
import org.stepik.android.model.SearchResult
import org.stepik.android.model.user.User
import ru.nobird.android.core.model.PagedList
import ru.nobird.android.core.model.mapPaged
import ru.nobird.android.core.model.mapToLongArray
import javax.inject.Inject

class CourseSearchInteractor
@Inject
constructor(
    private val searchResultRepository: SearchResultRepository,
    private val searchRepository: SearchRepository,
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,
    private val userRepository: UserRepository
) {
    fun getCourseSearchResult(courseId: Long, searchResultQuery: SearchResultQuery): Single<PagedList<CourseSearchResult>> =
        addSearchQuery(courseId, searchResultQuery) then
        searchResultRepository
            .getCourseSearchResults(courseId, searchResultQuery)
            .flatMap { searchResults ->
                fetchCourseSearchResultsDetails(searchResults)
            }

    private fun fetchCourseSearchResultsDetails(searchResults: PagedList<SearchResult>): Single<PagedList<CourseSearchResult>> {
        val lessonIds = searchResults
            .mapNotNull { it.lesson }
            .mapToLongArray { it }

        val lessonOwners = searchResults
            .mapNotNull { it.lessonOwner }
            .toSet()

        val commentOwners = searchResults
            .mapNotNull { it.commentUser }
            .toSet()

        val combined = (lessonOwners + commentOwners).toList()

        return lessonRepository
            .getLessons(*lessonIds, primarySourceType = DataSourceType.CACHE)
            .flatMap { lessons ->
                fetchProgressesAndUsers(searchResults, lessons, combined)
            }
    }

    private fun fetchProgressesAndUsers(searchResults: PagedList<SearchResult>, lessons: List<Lesson>, userIds: List<Long>): Single<PagedList<CourseSearchResult>> =
        zip(
            progressRepository.getProgresses(lessons.getProgresses(), primarySourceType = DataSourceType.REMOTE),
            userRepository.getUsers(userIds, primarySourceType = DataSourceType.REMOTE)
        ) { progresses, users ->
            val lessonMap = lessons.associateBy(Lesson::id)
            val progressMaps = progresses.associateBy(Progress::id)
            val userMaps = users.associateBy(User::id)

            searchResults.mapPaged { searchResult ->
                val lesson = searchResult.lesson?.let { lessonMap.getValue(it) }
                val lessonOwner = searchResult.lessonOwner?.let { userMaps[it] }
                val commentOwner = searchResult.commentUser?.let { userMaps[it] }

                CourseSearchResult(
                    searchResult = searchResult,
                    lesson = lesson,
                    progress = lesson?.progress?.let { progressMaps.getValue(it) },
                    lessonOwner = lessonOwner,
                    commentOwner = commentOwner
                )
            }
        }

    private fun addSearchQuery(courseId: Long, searchResultQuery: SearchResultQuery): Completable =
        if (searchResultQuery.query.isNullOrEmpty()) {
            Completable.complete()
        } else {
            searchRepository.saveSearchQuery(courseId = courseId, query = searchResultQuery.query)
        }
}