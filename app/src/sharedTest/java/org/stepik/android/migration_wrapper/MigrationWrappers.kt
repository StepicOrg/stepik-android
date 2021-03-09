package org.stepik.android.migration_wrapper

import org.stepic.droid.storage.migration.MigrationFrom62To63
import org.stepic.droid.storage.migration.Migrations
import org.stepik.android.cache.base.database.AppDatabase

object MigrationWrappers {
    private val oldMigrations =
        Migrations
            .migrations
            .slice(0 until AppDatabase.VERSION)
            .map { object : MigrationWrapper(it) {} } as List<MigrationWrapper>


    val allMigration = oldMigrations + MigrationWrapperFrom62To63(MigrationFrom62To63)
}