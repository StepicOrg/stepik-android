package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepik.android.model.Progress
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureProgress
import javax.inject.Inject

class ProgressDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<Progress>(databaseOperations) {

    public override fun parsePersistentObject(cursor: Cursor): Progress {
        val indexId = cursor.getColumnIndex(DbStructureProgress.Column.ID)
        val indexCost = cursor.getColumnIndex(DbStructureProgress.Column.COST)
        val indexScore = cursor.getColumnIndex(DbStructureProgress.Column.SCORE)
        val indexIsPassed = cursor.getColumnIndex(DbStructureProgress.Column.IS_PASSED)
        val indexLastViewed = cursor.getColumnIndex(DbStructureProgress.Column.LAST_VIEWED)
        val indexNSteps = cursor.getColumnIndex(DbStructureProgress.Column.N_STEPS)
        val indexNStepsPassed = cursor.getColumnIndex(DbStructureProgress.Column.N_STEPS_PASSED)

        return Progress(
            id      = cursor.getString(indexId),
            cost    = cursor.getInt(indexCost),
            score   = cursor.getString(indexScore),
            isPassed     = cursor.getInt(indexIsPassed) > 0,
            lastViewed   = cursor.getString(indexLastViewed),
            nStepsPassed = cursor.getInt(indexNStepsPassed),
            nSteps       = cursor.getInt(indexNSteps)
        )
    }

    public override fun getDbName(): String = DbStructureProgress.PROGRESS

    public override fun getContentValues(progress: Progress): ContentValues {
        val values = ContentValues()
        values.put(DbStructureProgress.Column.ID, progress.id)
        values.put(DbStructureProgress.Column.COST, progress.cost)
        values.put(DbStructureProgress.Column.SCORE, progress.score)
        values.put(DbStructureProgress.Column.IS_PASSED, progress.isPassed)
        values.put(DbStructureProgress.Column.LAST_VIEWED, progress.lastViewed)
        values.put(DbStructureProgress.Column.N_STEPS, progress.nSteps)
        values.put(DbStructureProgress.Column.N_STEPS_PASSED, progress.nStepsPassed)
        return values
    }

    public override fun getDefaultPrimaryColumn(): String = DbStructureProgress.Column.ID

    public override fun getDefaultPrimaryValue(persistentObject: Progress): String? =
            persistentObject.id

}
