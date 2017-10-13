package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.di.storage.StorageSingleton
import org.stepic.droid.model.code.CodeSubmission
import org.stepic.droid.storage.structure.DbStructureCodeSubmission
import javax.inject.Inject

@StorageSingleton
class CodeSubmissionDaoImpl
@Inject constructor(
        writeableDatabase: SQLiteDatabase) : DaoBase<CodeSubmission>(writeableDatabase) {

    override fun getDbName(): String = DbStructureCodeSubmission.CODE_SUBMISSION

    override fun getDefaultPrimaryColumn(): String = DbStructureCodeSubmission.Column.ATTEMPT_ID

    override fun getDefaultPrimaryValue(persistentObject: CodeSubmission): String = persistentObject.attemptId.toString()

    override fun getContentValues(persistentObject: CodeSubmission): ContentValues {
        val contentValues = ContentValues()

        contentValues.put(DbStructureCodeSubmission.Column.ATTEMPT_ID, persistentObject.attemptId)
        contentValues.put(DbStructureCodeSubmission.Column.STEP_ID, persistentObject.stepId)
        contentValues.put(DbStructureCodeSubmission.Column.CODE, persistentObject.code)
        contentValues.put(DbStructureCodeSubmission.Column.PROGRAMMING_LANGUAGE, persistentObject.language)

        return contentValues
    }

    override fun parsePersistentObject(cursor: Cursor): CodeSubmission {
        val attemptIdIndex = cursor.getColumnIndex(DbStructureCodeSubmission.Column.ATTEMPT_ID)
        val stepIdIndex = cursor.getColumnIndex(DbStructureCodeSubmission.Column.STEP_ID)
        val codeIndex = cursor.getColumnIndex(DbStructureCodeSubmission.Column.CODE)
        val programmingLanugageIndex = cursor.getColumnIndex(DbStructureCodeSubmission.Column.PROGRAMMING_LANGUAGE)

        return CodeSubmission(
                stepId = cursor.getLong(stepIdIndex),
                attemptId = cursor.getLong(attemptIdIndex),
                code = cursor.getString(codeIndex),
                language = cursor.getString(programmingLanugageIndex)
        )
    }

}
