package org.stepik.android.cache.base.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.stepik.android.migration_wrapper.MigrationWrappers
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @Test
    @Throws(IOException::class)
    fun migrateTest() {
        Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java)
            .addMigrations(*MigrationWrappers.allMigration.map { it.migration }.toTypedArray())
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    MigrationWrappers.allMigration.forEach {
                        it.migration.migrate(db)
                        it.runTest(db)
                    }
                }
            })
            .build()
            .openHelper
            .writableDatabase
    }
}