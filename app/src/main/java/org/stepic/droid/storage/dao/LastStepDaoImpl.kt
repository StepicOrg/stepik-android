package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureLastStep
import org.stepik.android.domain.last_step.model.LastStep
import javax.inject.Inject

class LastStepDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<LastStep>(databaseOperations) {
    override fun getDefaultPrimaryColumn()
            = DbStructureLastStep.Columns.ID

    override fun getDefaultPrimaryValue(persistentObject: LastStep) = persistentObject.id

    override fun getContentValues(persistentObject: LastStep): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DbStructureLastStep.Columns.ID, persistentObject.id)
        contentValues.put(DbStructureLastStep.Columns.UNIT_ID, persistentObject.unit)
        contentValues.put(DbStructureLastStep.Columns.STEP_ID, persistentObject.step)
        return contentValues
    }

    override fun parsePersistentObject(cursor: Cursor): LastStep =
            with(cursor) {
                val indexId = getColumnIndex(DbStructureLastStep.Columns.ID)
                val indexUnitId = getColumnIndex(DbStructureLastStep.Columns.UNIT_ID)
                val indexStepId = getColumnIndex(DbStructureLastStep.Columns.STEP_ID)

                val id = getString(indexId)
                val unitId = getLong(indexUnitId)
                val stepId = getLong(indexStepId)

                return LastStep(
                    id = id,
                    unit = unitId,
                    step = stepId
                )
            }

    override fun getDbName() = DbStructureLastStep.TABLE_NAME
}