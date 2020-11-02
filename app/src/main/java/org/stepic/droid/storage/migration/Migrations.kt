package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    private val oldMigration = arrayOf(
        object : Migration(0, 1) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom0To1(database)
            }
        },
        object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom1To2(database)
            }
        },
        object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom2To3(database)
            }
        },
        object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom3To4(database)
            }
        },
        object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom4To5(database)
            }
        },
        object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom5To6(database)
            }
        },
        object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom6To7(database)
            }
        },
        object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom7To8(database)
            }
        },
        object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom8To9(database)
            }
        },
        object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom9To10(database)
            }
        },
        object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        },
        object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom11To12(database)
            }
        },
        object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom12To13(database)
            }
        },
        object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom13To14(database)
            }
        },
        object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom14To15(database)
            }
        },
        object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom15To16(database)
            }
        },
        object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom16To17(database)
            }
        },
        object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom17To18(database)
            }
        },
        object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom18To19(database)
            }
        },
        object : Migration(19, 20) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        },
        object : Migration(20, 21) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        },
        object : Migration(21, 22) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom21To22(database)
            }
        },
        object : Migration(22, 23) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom22To23(database)
            }
        },
        object : Migration(23, 24) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom23To24(database)
            }
        },
        object : Migration(24, 25) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom24To25(database)
            }
        },
        object : Migration(25, 26) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        },
        object : Migration(26, 27) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom26To27(database)
            }
        },
        object : Migration(27, 28) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom27To28(database)
            }
        },
        object : Migration(28, 29) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom28To29(database)
            }
        },
        object : Migration(29, 30) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom29To30(database)
            }
        },
        object : Migration(30, 31) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom30To31(database)
            }
        },
        object : Migration(31, 32) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom31To32(database)
            }
        },
        object : Migration(32, 33) {
            override fun migrate(database: SupportSQLiteDatabase) {
                LegacyDatabaseMigrations.upgradeFrom32To33(database)
            }
        },
    )

    val migrations = oldMigration + arrayOf(
        MigrationFrom33To34,
        MigrationFrom34To35,
        MigrationFrom35To36,
        MigrationFrom36To37,
        MigrationFrom37To38,
        MigrationFrom38To39,
        MigrationFrom39To40,
        MigrationFrom40To41,
        MigrationFrom41To42,
        MigrationFrom42To43,
        MigrationFrom43To44,
        MigrationFrom44To45,
        MigrationFrom45To46,
        MigrationFrom46To47,
        MigrationFrom47To48,
        MigrationFrom48To49,
        MigrationFrom49To50,
        MigrationFrom50To51,
        MigrationFrom51To52,
        MigrationFrom52To53,
        MigrationFrom53To54,
        MigrationFrom54To55,
        MigrationFrom55To56,
        MigrationFrom56To57,
        MigrationFrom57To58,
        MigrationFrom58To59,
        MigrationFrom59To60,
        MigrationFrom60To61
    )
}