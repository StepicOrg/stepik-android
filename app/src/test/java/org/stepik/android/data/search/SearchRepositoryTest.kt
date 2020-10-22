package org.stepik.android.data.search


import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.stepic.droid.util.PagedList
import org.stepik.android.data.search_result.repository.SearchResultRepositoryImpl
import org.stepik.android.data.search_result.source.SearchResultRemoteDataSource
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.model.SearchResult

@RunWith(MockitoJUnitRunner::class)
class SearchRepositoryTest {
    @Mock
    private lateinit var searchResultRemoteDataSource: SearchResultRemoteDataSource

    @Test
    fun searchResultsLoadingTest() {
        val searchRepository = SearchResultRepositoryImpl(searchResultRemoteDataSource)

        val page = 1
        val rawQuery = "python"
        val lang = "en"

        val remoteResult = PagedList(listOf(mock(SearchResult::class.java)))

        whenever(searchResultRemoteDataSource.getSearchResults(
            SearchResultQuery(
                page = page,
                query = rawQuery,
                filterQuery = CourseListFilterQuery(language = lang)
            )
        )) doReturn Single.just(remoteResult)

        searchRepository
            .getSearchResults(
                SearchResultQuery(
                    page = page,
                    query = rawQuery,
                    filterQuery = CourseListFilterQuery(language = lang)
                )
            )
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(remoteResult)
    }
}