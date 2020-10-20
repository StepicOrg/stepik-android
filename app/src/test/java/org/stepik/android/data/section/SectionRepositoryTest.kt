package org.stepik.android.data.section

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
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

        whenever(sectionCacheDataSource.getSections(listOf(sectionId))) doReturn Single.just(listOf(section))
        whenever(sectionCacheDataSource.saveSections(any())) doReturn Completable.complete()
        whenever(sectionRemoteDataSource.getSections(any())) doReturn Single.just(emptyList())

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

        whenever(sectionCacheDataSource.getSections(listOf(sectionId))) doReturn Single.just(listOf(section))
        whenever(sectionRemoteDataSource.getSections(listOf(sectionId))) doReturn Single.error(IOException(""))

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
        val sectionList = listOf(section)

        whenever(sectionCacheDataSource.getSections(listOf(sectionId))) doReturn Single.just(emptyList())
        whenever(sectionCacheDataSource.saveSections(sectionList)) doReturn Completable.complete()
        whenever(sectionRemoteDataSource.getSections(listOf(sectionId))) doReturn Single.just(sectionList)

        sectionRepository
            .getSection(sectionId)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(section)

        verify(sectionCacheDataSource).saveSections(sectionList)
    }

    @Test
    fun sectionPartialLoadingTest() {
        val sectionRepository = SectionRepositoryImpl(sectionCacheDataSource, sectionRemoteDataSource)

        val sectionsIds = listOf(1L, 2L)
        val sections = sectionsIds.map { Section(id = it) }

        val cacheList = sections.subList(0, 1)
        val remoteList = sections.subList(1, 2)

        whenever(sectionCacheDataSource.getSections(sectionsIds)) doReturn Single.just(cacheList)
        whenever(sectionCacheDataSource.saveSections(remoteList)) doReturn Completable.complete()
        whenever(sectionRemoteDataSource.getSections(sectionsIds)) doReturn Single.just(emptyList())
        whenever(sectionRemoteDataSource.getSections(listOf(sectionsIds[1]))) doReturn Single.just(remoteList)

        sectionRepository
            .getSections(sectionsIds)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(sections)
    }
}