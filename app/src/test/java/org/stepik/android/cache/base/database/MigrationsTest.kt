package org.stepik.android.cache.base.database

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.storage.migration.Migrations
import org.stepik.android.migration_wrapper.MigrationWrappers

@RunWith(RobolectricTestRunner::class)
class MigrationsTest {
    /**
     * Migrations that we are covered by tests. Update this list, after writing
     * respective migration test in MigrationTest.kt
     */
    @Test
    fun migrationsPerformedTest() {
        val testedMigrations = MigrationWrappers.allMigration.map { it.migration }.toSet()
        assert(testedMigrations == Migrations.migrations.toSet()) { "Check if migration test is written and migrationNames is updated!" }
    }
}