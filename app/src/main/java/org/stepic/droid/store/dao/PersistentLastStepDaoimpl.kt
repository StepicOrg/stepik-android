package org.stepic.droid.store.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.model.PersistentLastStep
import org.stepic.droid.store.structure.DbStructureLastStep

class PersistentLastStepDaoImpl(writableDatabase: SQLiteDatabase) : DaoBase<PersistentLastStep>(writableDatabase) {
    override fun getDefaultPrimaryColumn()
            = DbStructureLastStep.Column.COURSE_ID;

    override fun getDefaultPrimaryValue(persistentObject: PersistentLastStep) = persistentObject.courseId.toString()

    override fun getContentValues(persistentObject: PersistentLastStep): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DbStructureLastStep.Column.COURSE_ID, persistentObject.courseId)
        contentValues.put(DbStructureLastStep.Column.UNIT_ID, persistentObject.unitId)
        contentValues.put(DbStructureLastStep.Column.STEP_ID, persistentObject.stepId)
        contentValues.put(DbStructureLastStep.Column.TIMESTAMP, persistentObject.timestamp)
        return contentValues
    }

    override fun parsePersistentObject(cursor: Cursor): PersistentLastStep {
        val indexCourseId = cursor.getColumnIndex(DbStructureLastStep.Column.COURSE_ID)
        val indexUnitId = cursor.getColumnIndex(DbStructureLastStep.Column.UNIT_ID)
        val indexStepId = cursor.getColumnIndex(DbStructureLastStep.Column.STEP_ID)
        val indexTimestamp = cursor.getColumnIndex(DbStructureLastStep.Column.TIMESTAMP)

        val timestampSource: Long = cursor.getLong(indexTimestamp)
        val timestamp = if (timestampSource <= 0) {
            null
        } else {
            timestampSource
        }
        val courseId = cursor.getLong(indexCourseId)
        val unitId = cursor.getLong(indexUnitId)
        val stepId = cursor.getLong(indexStepId)

        return PersistentLastStep(
                courseId = courseId,
                unitId = unitId,
                stepId = stepId,
                timestamp = timestamp
        )
    }

    override fun getDbName() = DbStructureLastStep.LAST_STEPS
}