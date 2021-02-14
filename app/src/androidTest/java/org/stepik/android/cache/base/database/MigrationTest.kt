package org.stepik.android.cache.base.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.runner.RunWith

/**
 * File with migration tests
 * Tests run migrations from Migrations.kt and validate the result using the json
 * schema generated from AppDatabase
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

//    @Test
//    @Throws(IOException::class)
//    fun migrate63To64() {
//        helper.createDatabase(TEST_DB, 63).apply {
//            close()
//        }
//
//        helper.runMigrationsAndValidate(TEST_DB, 64, true, MigrationFrom63To64)
//    }
}