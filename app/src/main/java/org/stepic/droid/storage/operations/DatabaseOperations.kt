package org.stepic.droid.storage.operations

import android.content.ContentValues

interface DatabaseOperations {
    fun <U> executeQuery(sqlQuery: String?, selectionArgs: Array<String>?, handler: ResultHandler<U>): U
    fun executeSql(sqlQuery: String, args: Array<Any>)

    fun executeUpdate(table: String, values: ContentValues?, whereClause: String?, whereArgs: Array<String>?)

    fun executeInsert(table: String, values: ContentValues?)

    fun executeReplace(table: String, values: ContentValues?)
    fun executeReplaceAll(table: String, values: List<ContentValues>)

    fun executeDelete(table: String, whereClause: String?, whereArgs: Array<String>?)
}
