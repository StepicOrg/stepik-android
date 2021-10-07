package org.stepik.android.domain.course_search.interactor

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_search.model.CourseSearchResult
import org.stepik.android.domain.course_search.model.CourseSearchResultListItem
import org.stepik.android.domain.discussion_thread.repository.DiscussionThreadRepository
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.search.repository.SearchRepository
import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.domain.search_result.repository.SearchResultRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.Progress
import org.stepik.android.model.Step
import org.stepik.android.model.Lesson
import org.stepik.android.model.Unit
import org.stepik.android.model.Section
import org.stepik.android.model.SearchResult
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.model.user.User
import ru.nobird.android.core.model.PagedList
import ru.nobird.android.core.model.mapPaged
import javax.inject.Inject

class CourseSearchInteractor
@Inject
constructor(
    private val searchResultRepository: SearchResultRepository,
    private val searchRepository: SearchRepository,
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,
    private val userRepository: UserRepository,
    private val unitRepository: UnitRepository,
    private val sectionRepository: SectionRepository,
    private val stepRepository: StepRepository,
    private val discussionThreadRepository: DiscussionThreadRepository
) {
    fun addSearchQuery(courseId: Long, searchResultQuery: SearchResultQuery): Completable =
        if (searchResultQuery.query.isNullOrEmpty()) {
            Completable.complete()
        } else {
            searchRepository.saveSearchQuery(courseId = courseId, query = searchResultQuery.query)
        }

    fun getCourseSearchResult(searchResultQuery: SearchResultQuery): Flowable<PagedList<CourseSearchResultListItem.Data>> =
        searchResultRepository
            .getSearchResults(searchResultQuery)
            .flatMapPublisher { searchResults ->
                val lessonIds = searchResults
                    .mapNotNull { it.lesson }
                    .toLongArray()
                lessonRepository
                    .getLessons(*lessonIds, primarySourceType = DataSourceType.REMOTE)
                    .flatMapPublisher { lessons ->
                        val lessonsMap = lessons.associateBy(Lesson::id)
                        val courseSearchResults = searchResults.mapPaged { searchResult ->
                            CourseSearchResultListItem.Data(
                                CourseSearchResult(
                                    searchResult = searchResult,
                                    lesson = searchResult.lesson?.let { lessonsMap[it] }
                                )
                            )
                        }
                        Single.concat(Single.just(courseSearchResults), fetchCourseSearchResultsDetails(searchResults, lessons))
                    }
            }

    fun getDiscussionThreads(step: Step): Single<List<DiscussionThread>> =
        discussionThreadRepository
            .getDiscussionThreads(*step.discussionThreads?.toTypedArray() ?: arrayOf())

    private fun fetchCourseSearchResultsDetails(searchResults: PagedList<SearchResult>, lessons: List<Lesson>): Single<PagedList<CourseSearchResultListItem.Data>> {
        val unitIds = lessons.flatMap { lesson -> lesson.units }

        val commentOwners = searchResults
            .mapNotNull { it.commentUser }

        return Singles.zip(
            unitRepository.getUnits(unitIds, DataSourceType.REMOTE),
            userRepository.getUsers(commentOwners, primarySourceType = DataSourceType.REMOTE)
        ).flatMap { (units, users) ->
            fetchProgressesAndUsers(searchResults, lessons, units, users)
        }
    }

    private fun fetchProgressesAndUsers(searchResults: PagedList<SearchResult>, lessons: List<Lesson>, units: List<Unit>, users: List<User>): Single<PagedList<CourseSearchResultListItem.Data>> =
        Singles.zip(
            progressRepository.getProgresses(lessons.getProgresses(), primarySourceType = DataSourceType.REMOTE),
            sectionRepository.getSections(units.map(Unit::section), primarySourceType = DataSourceType.REMOTE)
        ) { progresses, sections ->
            val lessonMap = lessons.associateBy(Lesson::id)
            val progressMaps = progresses.associateBy(Progress::id)
            val userMaps = users.associateBy(User::id)
            val unitsMap = units.associateBy(Unit::lesson)
            val sectionsMap = sections.associateBy(Section::id)

            searchResults.mapPaged { searchResult ->
                val lesson = searchResult.lesson?.let { lessonMap.getValue(it) }
                val commentOwner = searchResult.commentUser?.let { userMaps[it] }
                val unit = searchResult.lesson?.let { unitsMap[it] }
                val section = unit?.section?.let { sectionsMap[it] }

                CourseSearchResultListItem.Data(
                    CourseSearchResult(
                        searchResult = searchResult,
                        lesson = lesson,
                        progress = lesson?.progress?.let { progressMaps.getValue(it) },
                        unit = unit,
                        section = section,
                        commentOwner = commentOwner
                    )
                )
            }
        }
}