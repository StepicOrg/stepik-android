package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.model.PersistentLastStep
import org.stepic.droid.storage.operations.CrudOperations
import org.stepic.droid.storage.structure.DbStructureLastStep
import javax.inject.Inject

class PersistentLastStepDaoImpl @Inject constructor(crudOperations: CrudOperations) : DaoBase<PersistentLastStep>(crudOperations) {
    override fun getDefaultPrimaryColumn()
            = DbStructureLastStep.Column.COURSE_ID

    override fun getDefaultPrimaryValue(persistentObject: PersistentLastStep) = persistentObject.courseId.toString()

    override fun getContentValues(persistentObject: PersistentLastStep): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DbStructureLastStep.Column.COURSE_ID, persistentObject.courseId)
        contentValues.put(DbStructureLastStep.Column.UNIT_ID, persistentObject.unitId)
        contentValues.put(DbStructureLastStep.Column.STEP_ID, persistentObject.stepId)
        return contentValues
    }

    override fun parsePersistentObject(cursor: Cursor): PersistentLastStep {
        val indexCourseId = cursor.getColumnIndex(DbStructureLastStep.Column.COURSE_ID)
        val indexUnitId = cursor.getColumnIndex(DbStructureLastStep.Column.UNIT_ID)
        val indexStepId = cursor.getColumnIndex(DbStructureLastStep.Column.STEP_ID)

        val courseId = cursor.getLong(indexCourseId)
        val unitId = cursor.getLong(indexUnitId)
        val stepId = cursor.getLong(indexStepId)

        return PersistentLastStep(
                courseId = courseId,
                unitId = unitId,
                stepId = stepId)
    }

    override fun getDbName() = DbStructureLastStep.LAST_STEPS
}