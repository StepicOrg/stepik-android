package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getDate
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.assignment.structure.DbStructureAssignment
import org.stepik.android.model.Assignment
import javax.inject.Inject

class AssignmentDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<Assignment>(databaseOperations) {
    public override fun getDbName(): String =
        DbStructureAssignment.TABLE_NAME

    public override fun parsePersistentObject(cursor: Cursor): Assignment =
        Assignment(
            id = cursor.getLong(DbStructureAssignment.Columns.ID),
            step = cursor.getLong(DbStructureAssignment.Columns.STEP),
            unit = cursor.getLong(DbStructureAssignment.Columns.UNIT),
            progress = cursor.getString(DbStructureAssignment.Columns.PROGRESS),

            createDate = cursor.getDate(DbStructureAssignment.Columns.CREATE_DATE),
            updateDate = cursor.getDate(DbStructureAssignment.Columns.UPDATE_DATE)
        )

    public override fun getContentValues(assignment: Assignment): ContentValues {
        val values = ContentValues()

        values.put(DbStructureAssignment.Columns.ID, assignment.id)
        values.put(DbStructureAssignment.Columns.PROGRESS, assignment.progress)
        values.put(DbStructureAssignment.Columns.STEP, assignment.step)
        values.put(DbStructureAssignment.Columns.UNIT, assignment.unit)
        values.put(DbStructureAssignment.Columns.CREATE_DATE, assignment.createDate?.time ?: -1)
        values.put(DbStructureAssignment.Columns.UPDATE_DATE, assignment.updateDate?.time ?: -1)

        return values
    }

    public override fun getDefaultPrimaryColumn(): String =
            DbStructureAssignment.Columns.ID

    public override fun getDefaultPrimaryValue(persistentObject: Assignment): String =
            persistentObject.id.toString()
}
