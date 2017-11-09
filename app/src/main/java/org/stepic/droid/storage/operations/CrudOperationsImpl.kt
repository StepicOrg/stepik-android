package org.stepic.droid.storage.operations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.di.storage.StorageSingleton
import org.stepic.droid.util.RWLocks
import javax.inject.Inject

@StorageSingleton
class CrudOperationsImpl
@Inject
constructor(private val database: SQLiteDatabase) : CrudOperations {

    private fun open() {
        RWLocks.DatabaseLock.writeLock().lock()
    }

    private fun close() {
        RWLocks.DatabaseLock.writeLock().unlock()
    }

    private fun openRead() {
        RWLocks.DatabaseLock.readLock().lock()
    }

    private fun closeRead() {
        RWLocks.DatabaseLock.readLock().unlock()
    }

    override fun <U> executeQuery(sqlQuery: String?, selectionArgs: Array<String>?, handler: ResultHandler<U>): U {
        try {
            openRead()
            val cursor = database.rawQuery(sqlQuery, selectionArgs)
            return try {
                handler.handle(cursor)
            } finally {
                cursor.close()
            }
        } finally {
            closeRead()
        }
    }


    override fun executeUpdate(table: String, values: ContentValues?, whereClause: String?, whereArgs: Array<String>?) {
        try {
            open()
            database.update(table, values, whereClause, whereArgs)
        } finally {
            close()
        }
    }

    override fun executeInsert(table: String, values: ContentValues?) {
        try {
            open()
            database.insert(table, null, values)
        } finally {
            close()
        }
    }

    override fun executeReplace(table: String, values: ContentValues?) {
        try {
            open()
            database.replace(table, null, values)
        } finally {
            close()
        }
    }

    override fun executeDelete(table: String, whereClause: String?, whereArgs: Array<String>?) {
        try {
            open()
            database.delete(table, whereClause, whereArgs)
        } finally {
            close()
        }
    }

}
