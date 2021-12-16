package org.stepik.android.migration_wrapper

import org.stepic.droid.storage.migration.*

object MigrationWrappers {
    private const val LAST_TESTED_DATABASE_VERSION = 63
    private val oldMigrations =
        Migrations
            .migrations
            .slice(0 until LAST_TESTED_DATABASE_VERSION - 1)
            .map { object : MigrationWrapper(it) {} } as List<MigrationWrapper>


    val allMigration = oldMigrations + MigrationWrapperFrom62To63(MigrationFrom62To63) +
            listOf(
                object : MigrationWrapper(MigrationFrom63To64) {},
                object : MigrationWrapper(MigrationFrom64To65) {},
                object : MigrationWrapper(MigrationFrom65To66) {},
                object : MigrationWrapper(MigrationFrom66To67) {},
                object : MigrationWrapper(MigrationFrom67To68) {},
                MigrationWrapperFrom68To69(MigrationFrom68To69),
                object : MigrationWrapper(MigrationFrom69To70) {},
                // TODO Multiple tests on a single table fail, must research
                object : MigrationWrapper(MigrationFrom70To71) {},
                object : MigrationWrapper(MigrationFrom71To72) {},
                object : MigrationWrapper(MigrationFrom72To73) {},
                object : MigrationWrapper(MigrationFrom73To74) {},
                object : MigrationWrapper(MigrationFrom74To75) {},
                object : MigrationWrapper(MigrationFrom75To76) {},
                object : MigrationWrapper(MigrationFrom76To77) {}
            )
}