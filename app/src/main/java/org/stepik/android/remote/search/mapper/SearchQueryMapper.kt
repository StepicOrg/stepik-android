package org.stepik.android.remote.search.mapper

import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.putNullable
import org.stepik.android.domain.course_list.model.SearchQuery
import javax.inject.Inject

class SearchQueryMapper
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    companion object {
        private const val PAGE = "page"
        private const val TAG = "tag"
        private const val QUERY = "query"
        private const val LANG = "language"
    }

    fun mapToQueryMap(searchQuery: SearchQuery): Map<String, String> {
        val mutableMap = hashMapOf<String, String>()

        mutableMap.putNullable(PAGE, searchQuery.page?.toString())
        mutableMap.putNullable(TAG, searchQuery.tag?.toString())
        mutableMap.putNullable(QUERY, searchQuery.query)
        mutableMap.putNullable(LANG, sharedPreferenceHelper.languageForFeatured)

        return mutableMap
    }
}