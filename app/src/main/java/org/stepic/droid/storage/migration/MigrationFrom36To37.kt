package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureLastStep
import org.stepic.droid.storage.structure.DbStructureLastStepOld

object MigrationFrom36To37 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE ${DbStructureLastStepOld.LAST_STEPS}")
        DbStructureLastStep.createTable(db)
    }
}