package org.stepik.android.remote.search_result.mapper

import org.stepik.android.domain.search_result.model.SearchResultQuery
import ru.nobird.android.core.model.mapOfNotNull
import javax.inject.Inject

class SearchResultQueryMapper
@Inject
constructor() {
    companion object {
        private const val PAGE = "page"
        private const val TAG = "tag"
        private const val QUERY = "query"
        private const val LANG = "language"
        private const val IS_PAID = "is_paid"
        private const val WITH_CERTIFICATE = "with_certificate"
    }

    fun mapToQueryMap(searchResultQuery: SearchResultQuery): Map<String, String> =
        mapOfNotNull(
            PAGE to searchResultQuery.page?.toString(),
            TAG to searchResultQuery.tagId?.toString(),
            QUERY to searchResultQuery.query,
            LANG to searchResultQuery.filterQuery?.language,
            IS_PAID to searchResultQuery.filterQuery?.isPaid?.toString(),
            WITH_CERTIFICATE to searchResultQuery.filterQuery?.withCertificate?.toString()
        )
}