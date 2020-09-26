package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureProgress
import org.stepic.droid.util.getBoolean
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.model.Progress
import javax.inject.Inject

class ProgressDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<Progress>(databaseOperations) {
    public override fun getDbName(): String =
        DbStructureProgress.TABLE_NAME

    public override fun getDefaultPrimaryColumn(): String =
        DbStructureProgress.Columns.ID

    public override fun getDefaultPrimaryValue(persistentObject: Progress): String? =
        persistentObject.id

    public override fun parsePersistentObject(cursor: Cursor): Progress =
        Progress(
            id      = cursor.getString(DbStructureProgress.Columns.ID).orEmpty(),
            cost    = cursor.getLong(DbStructureProgress.Columns.COST),
            score   = cursor.getString(DbStructureProgress.Columns.SCORE),
            isPassed     = cursor.getBoolean(DbStructureProgress.Columns.IS_PASSED),
            lastViewed   = cursor.getString(DbStructureProgress.Columns.LAST_VIEWED),
            nStepsPassed = cursor.getLong(DbStructureProgress.Columns.N_STEPS_PASSED),
            nSteps       = cursor.getLong(DbStructureProgress.Columns.N_STEPS)
        )

    public override fun getContentValues(progress: Progress): ContentValues {
        val values = ContentValues()
        values.put(DbStructureProgress.Columns.ID, progress.id)
        values.put(DbStructureProgress.Columns.COST, progress.cost)
        values.put(DbStructureProgress.Columns.SCORE, progress.score)
        values.put(DbStructureProgress.Columns.IS_PASSED, progress.isPassed)
        values.put(DbStructureProgress.Columns.LAST_VIEWED, progress.lastViewed)
        values.put(DbStructureProgress.Columns.N_STEPS, progress.nSteps)
        values.put(DbStructureProgress.Columns.N_STEPS_PASSED, progress.nStepsPassed)
        return values
    }
}
