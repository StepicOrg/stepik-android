package org.stepik.android.data.section

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner
import org.stepik.android.data.section.repository.SectionRepositoryImpl
import org.stepik.android.data.section.source.SectionCacheDataSource
import org.stepik.android.data.section.source.SectionRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Section
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class SectionRepositoryTest {
    @Mock
    private lateinit var sectionRemoteDataSource: SectionRemoteDataSource

    @Mock
    private lateinit var sectionCacheDataSource: SectionCacheDataSource

    @Test
    fun sectionInCacheTest() {
        val sectionRepository = SectionRepositoryImpl(sectionCacheDataSource, sectionRemoteDataSource)

        val sectionId = 312L
        val section = Section(id = sectionId)

        whenever(sectionCacheDataSource.getSection(sectionId)) doReturn Maybe.just(section)
        whenever(sectionRemoteDataSource.getSection(sectionId)) doReturn Maybe.empty()

        sectionRepository
            .getSection(sectionId, primarySourceType = DataSourceType.CACHE)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(section)
    }

    @Test
    fun sectionRemoteFallbackToCacheTest() {
        val sectionRepository = SectionRepositoryImpl(sectionCacheDataSource, sectionRemoteDataSource)

        val sectionId = 312L
        val section = Section(id = sectionId)

        whenever(sectionCacheDataSource.getSection(sectionId)) doReturn Maybe.just(section)
        whenever(sectionRemoteDataSource.getSection(sectionId)) doReturn Maybe.error(IOException(""))

        sectionRepository
            .getSection(sectionId, primarySourceType = DataSourceType.REMOTE)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(section)
    }

    @Test
    fun sectionEmptyCacheSwitchToRemoteTest() {
        val sectionRepository = SectionRepositoryImpl(sectionCacheDataSource, sectionRemoteDataSource)

        val sectionId = 312L
        val section = Section(id = sectionId)

        whenever(sectionCacheDataSource.getSection(sectionId)) doReturn Maybe.empty()
        whenever(sectionCacheDataSource.saveSection(section)) doReturn Completable.complete()
        whenever(sectionRemoteDataSource.getSection(sectionId)) doReturn Maybe.just(section)

        sectionRepository
            .getSection(sectionId)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(section)

        verify(sectionCacheDataSource).saveSection(section)
    }
}