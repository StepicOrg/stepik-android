package org.stepic.droid.storage.operations

import android.content.ContentValues

interface CrudOperations {
    fun <U> executeQuery(sqlQuery: String?, selectionArgs: Array<String>?, handler: ResultHandler<U>): U

    fun executeUpdate(table: String, values: ContentValues?, whereClause: String?, whereArgs: Array<String>?)

    fun executeInsert(table: String, values: ContentValues?)

    fun executeDelete(table: String, whereClause: String?, whereArgs: Array<String>?)
}
