package org.stepik.android.cache.mobile_tiers

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.stepik.android.cache.testutil.InMemoryBillingDatabaseTest
import org.stepik.android.cache.testutil.awaitComplete
import org.stepik.android.cache.testutil.awaitValue
import org.stepik.android.domain.mobile_tiers.model.MobileTier

@RunWith(AndroidJUnit4::class)
class MobileTiersDaoTest : InMemoryBillingDatabaseTest() {
    @Test
    fun roundTripsEveryCombinationOfNullableStandardAndPromoTiers() {
        val items = listOf(
            MobileTier("test-tier-1", 4_000_000_001L, "test-standard", "test-promo"),
            MobileTier("test-tier-2", 2L, "test-standard", null),
            MobileTier("test-tier-3", 3L, null, "test-promo"),
            MobileTier("test-tier-4", 4L, null, null)
        )
        val dao = database.mobileTiersDao()
        dao.saveMobileTiers(items).awaitComplete()
        assertEquals(items.toSet(), dao.getMobileTiers(items.map { it.course }).awaitValue().toSet())
    }

    @Test
    fun courseIsPrimaryKeyAndDifferentCoursesCanShareTierId() {
        val dao = database.mobileTiersDao()
        val first = MobileTier("test-shared-tier", 1L, "test-price-1", "test-promo-1")
        val other = first.copy(course = 2L)
        dao.saveMobileTiers(listOf(first, other)).awaitComplete()
        assertEquals(setOf(first, other), dao.getMobileTiers(listOf(1L, 2L)).awaitValue().toSet())
        val replacement = MobileTier("test-new-tier", first.course, null, "test-new-promo")
        dao.saveMobileTiers(listOf(replacement)).awaitComplete()
        assertEquals(setOf(replacement, other), dao.getMobileTiers(listOf(1L, 2L)).awaitValue().toSet())
    }

    @Test
    fun requestedMissingEmptyAndDuplicateIdsReturnOnlyExistingCoursesOnce() {
        val dao = database.mobileTiersDao()
        val items = listOf(MobileTier("test-tier-1", 1L), MobileTier("test-tier-2", 2L))
        dao.saveMobileTiers(items + items.first()).awaitComplete()
        assertEquals(listOf(items.first()), dao.getMobileTiers(listOf(1L, 1L, 99L)).awaitValue())
        assertEquals(emptyList<MobileTier>(), dao.getMobileTiers(listOf(99L)).awaitValue())
        assertEquals(emptyList<MobileTier>(), dao.getMobileTiers(emptyList()).awaitValue())
        assertEquals(2, dao.getMobileTiers(listOf(1L, 2L)).awaitValue().size)
    }

    @Test
    fun realCacheRoundTripAndEmptySavePreserveRows() {
        val cache = MobileTiersCacheDataSourceImpl(database.mobileTiersDao())
        val item = MobileTier("test-tier", 1L, "test-price", null)
        cache.saveMobileTiers(listOf(item)).awaitComplete()
        cache.saveMobileTiers(emptyList()).awaitComplete()
        assertEquals(listOf(item), cache.getMobileTiers(listOf(1L, 99L)).awaitValue())
    }
}
