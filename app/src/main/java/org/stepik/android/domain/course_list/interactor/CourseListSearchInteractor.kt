package org.stepik.android.domain.course_list.interactor

import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.PagedList
import ru.nobird.android.core.model.mapToLongArray
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.domain.search_result.repository.SearchResultRepository
import org.stepik.android.model.SearchResult
import javax.inject.Inject

class CourseListSearchInteractor
@Inject
constructor(
    private val searchResultRepository: SearchResultRepository,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val courseListInteractor: CourseListInteractor
) {
    fun getCoursesBySearch(searchResultQuery: SearchResultQuery): Observable<Pair<PagedList<CourseListItem.Data>, DataSourceType>> =
        searchResultRepository
            .getSearchResults(searchResultQuery.copy(lang = sharedPreferenceHelper.languageForFeatured))
            .flatMapObservable { searchResult ->
                val ids = searchResult.mapToLongArray(SearchResult::course)
                Single
                    .concat(
                        getCourseListItems(ids, searchResult, searchResultQuery, DataSourceType.CACHE),
                        getCourseListItems(ids, searchResult, searchResultQuery, DataSourceType.REMOTE)
                    )
                    .toObservable()
            }

    private fun getCourseListItems(
        courseIds: LongArray,
        searchResult: PagedList<SearchResult>,
        searchResultQuery: SearchResultQuery,
        sourceType: DataSourceType
    ): Single<Pair<PagedList<CourseListItem.Data>, DataSourceType>> =
        courseListInteractor
            .getCourseListItems(*courseIds, courseViewSource = CourseViewSource.Search(searchResultQuery), sourceType = sourceType)
            .map { courseListItems ->
                PagedList(
                    list = courseListItems,
                    page = searchResult.page,
                    hasPrev = searchResult.hasPrev,
                    hasNext = searchResult.hasNext
                ) to sourceType
            }
}