package org.stepik.android.domain.course_list.interactor

import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.domain.search_result.repository.SearchResultRepository
import javax.inject.Inject

class CourseListSearchInteractor
@Inject
constructor(
    private val searchResultRepository: SearchResultRepository,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val courseListInteractor: CourseListInteractor
) {
    fun getCoursesBySearch(searchResultQuery: SearchResultQuery): Single<PagedList<CourseListItem.Data>> =
        searchResultRepository
            .getSearchResults(searchResultQuery.copy(lang = sharedPreferenceHelper.languageForFeatured))
            .flatMap { searchResult ->
                courseListInteractor
                    .getCourseListItems(*searchResult.mapToLongArray { it.course })
                    .map { courseListItems ->
                        PagedList(
                            list = courseListItems,
                            page = searchResult.page,
                            hasPrev = searchResult.hasPrev,
                            hasNext = searchResult.hasNext
                        )
                    }
            }
}