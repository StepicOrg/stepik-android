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
import org.stepik.android.data.search.repository.SearchRepositoryImpl
import org.stepik.android.data.search.source.SearchRemoteDataSource
import org.stepik.android.model.SearchResult

@RunWith(MockitoJUnitRunner::class)
class SearchRepositoryTest {
    @Mock
    private lateinit var searchRemoteDataSource: SearchRemoteDataSource

    @Test
    fun searchResultsLoadingTest() {
        val searchRepository = SearchRepositoryImpl(searchRemoteDataSource)

        val page = 1
        val rawQuery = "python"
        val lang = "en"

        val remoteResult = PagedList(listOf(mock(SearchResult::class.java)))

        whenever(searchRemoteDataSource.getSearchResultsCourses(page, rawQuery, lang)) doReturn Single.just(remoteResult)

        searchRepository
            .getSearchResultsCourses(page, rawQuery, lang)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(remoteResult)
    }
}