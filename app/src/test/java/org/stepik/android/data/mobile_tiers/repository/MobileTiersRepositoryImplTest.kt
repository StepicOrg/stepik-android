package org.stepik.android.data.mobile_tiers.repository

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.verifyNoInteractions
import org.stepik.android.data.mobile_tiers.source.MobileTiersCacheDataSource
import org.stepik.android.data.mobile_tiers.source.MobileTiersRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation

class MobileTiersRepositoryImplTest {
    private val remote = mock<MobileTiersRemoteDataSource>()
    private val cache = mock<MobileTiersCacheDataSource>()
    private val repository = MobileTiersRepositoryImpl(remote, cache)
    private val calculations = listOf(MobileTierCalculation(42, promo = "ПРОМО"), MobileTierCalculation(43, store = "synthetic-store", promo = "SECOND"))
    private val first = MobileTier("tier-1", 42, "sku-1", "promo-1")
    private val second = MobileTier("tier-2", 43, "sku-2")
    private val ids = listOf(42L, 43L)
    private val failure = IllegalStateException("synthetic failure")
    private val events = mutableListOf<String>()

    private fun setup(cached: List<MobileTier> = listOf(first), fetched: List<MobileTier> = listOf(first, second), remoteError: Throwable? = null, readError: Throwable? = null, saveError: Throwable? = null) {
        whenever(cache.getMobileTiers(ids)).thenReturn(Single.defer {
            events += "read"
            if (readError == null) Single.just(cached) else Single.error(readError)
        })
        whenever(remote.getMobileTiers(any())).thenAnswer {
            Single.defer {
                events += "remote"
                if (remoteError == null) Single.just(fetched) else Single.error<List<MobileTier>>(remoteError)
            }
        }
        whenever(cache.saveMobileTiers(any())).thenAnswer {
            Completable.defer { events += "save"; if (saveError == null) Completable.complete() else Completable.error(saveError) }
        }
    }

    @Test fun emptyInputSkipsBothSourcesForEveryMode() {
        for (mode in DataSourceType.entries) repository.calculateMobileTiers(emptyList(), mode).test().assertResult(emptyList())
        verifyNoInteractions(remote, cache)
    }

    @Test fun remoteForwardsCalculationsAndWaitsForSaveWithoutReadingCache() {
        setup()
        val save = CompletableSubject.create()
        whenever(cache.saveMobileTiers(listOf(first, second))).thenReturn(save)
        val observer = repository.calculateMobileTiers(calculations, DataSourceType.REMOTE).test()
        try {
            observer.assertNoValues().assertNotComplete()
            assertEquals(listOf("remote"), events)
            assertTrue(save.hasObservers())
            save.onComplete()
            observer.assertResult(listOf(first, second))
            verify(remote).getMobileTiers(calculations)
        } finally { observer.dispose() }
    }

    @Test fun remoteAndSaveFailuresBothFallBackToCache() {
        for (writeFailure in listOf(false, true)) {
            setup(remoteError = if (writeFailure) null else failure, saveError = if (writeFailure) failure else null)
            events.clear()
            repository.calculateMobileTiers(calculations, DataSourceType.REMOTE).test().assertResult(listOf(first))
            assertEquals(if (writeFailure) listOf("remote", "save", "read") else listOf("remote", "read"), events)
        }
    }

    @Test fun fallbackReadFailurePropagates() {
        val readFailure = IllegalArgumentException("cache unavailable")
        setup(remoteError = failure, readError = readFailure)
        repository.calculateMobileTiers(calculations, DataSourceType.REMOTE).test().assertNoValues().assertError { it === readFailure }
        assertEquals(listOf("remote", "read"), events)
    }

    @Test fun cacheMissPreservesOriginalPromoAndStoreAndMergesOnlyMissingCourses() {
        setup(fetched = listOf(second))
        repository.calculateMobileTiers(calculations, DataSourceType.CACHE).test().assertResult(listOf(first, second))
        verify(remote).getMobileTiers(listOf(calculations[1]))
        verify(cache).saveMobileTiers(listOf(second))
        assertEquals(listOf("read", "remote", "save"), events)
    }

    @Test fun fullCacheHitHasNoRemoteSubscriptionOrWrite() {
        setup(cached = listOf(second, first))
        repository.calculateMobileTiers(calculations, DataSourceType.CACHE).test().assertResult(listOf(second, first))
        assertEquals(listOf("read"), events)
        verify(cache, never()).saveMobileTiers(any())
    }

    @Test fun partialAndEmptyRemoteResultsRemainPartialInBothModes() {
        for (mode in DataSourceType.values()) for (result in listOf(emptyList(), listOf(second))) {
            setup(fetched = result)
            repository.calculateMobileTiers(calculations, mode).test()
                .assertResult(if (mode == DataSourceType.CACHE) listOf(first) + result else result)
        }
    }

    @Test fun cacheReadFailureDoesNotSubscribeToRemote() {
        setup(readError = failure)
        repository.calculateMobileTiers(calculations, DataSourceType.CACHE).test().assertNoValues().assertError { it === failure }
        assertEquals(listOf("read"), events)
    }

    @Test fun cacheMissRemoteAndSaveFailuresPropagateWithoutSecondCacheRead() {
        for (writeFailure in listOf(false, true)) {
            setup(remoteError = if (writeFailure) null else failure, saveError = if (writeFailure) failure else null)
            events.clear()
            repository.calculateMobileTiers(calculations, DataSourceType.CACHE).test().assertNoValues().assertError { it === failure }
            assertEquals(if (writeFailure) listOf("read", "remote", "save") else listOf("read", "remote"), events)
        }
    }
}
