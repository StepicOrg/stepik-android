package org.stepic.droid.persistence.storage.dao

import android.content.ContentValues
import android.database.Cursor
import io.reactivex.Maybe
import io.reactivex.Observable
import org.stepic.droid.di.storage.StorageSingleton
import org.stepic.droid.persistence.model.DownloadTask
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.dao.IDao
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getBoolean
import javax.inject.Inject

@StorageSingleton
class PersistentItemDaoImpl
@Inject
constructor(
        databaseOperations: DatabaseOperations
): DaoBase<PersistentItem>(databaseOperations), IDao<PersistentItem>, PersistentItemDao {
    override fun getDbName() = DBStructurePersistentItem.PERSISTENT_ITEMS

    override fun getDefaultPrimaryColumn() = DBStructurePersistentItem.Columns.ORIGINAL_PATH // actually ORIGINAL_PATH + STEP
    override fun getDefaultPrimaryValue(persistentObject: PersistentItem): String = persistentObject.task.originalPath

    override fun getContentValues(persistentObject: PersistentItem) = ContentValues().apply {
        put(DBStructurePersistentItem.Columns.ORIGINAL_PATH, persistentObject.task.originalPath)
        put(DBStructurePersistentItem.Columns.LOCAL_FILE_NAME, persistentObject.localFileName)
        put(DBStructurePersistentItem.Columns.LOCAL_FILE_DIR, persistentObject.localFileDir)
        put(DBStructurePersistentItem.Columns.IS_IN_APP_INTERNAL_DIR, if (persistentObject.isInAppInternalDir) 1 else 0)

        put(DBStructurePersistentItem.Columns.DOWNLOAD_ID, persistentObject.downloadId)
        put(DBStructurePersistentItem.Columns.STATUS, persistentObject.status.name)

        put(DBStructurePersistentItem.Columns.COURSE, persistentObject.task.course)
        put(DBStructurePersistentItem.Columns.SECTION, persistentObject.task.section)
        put(DBStructurePersistentItem.Columns.UNIT, persistentObject.task.unit)
        put(DBStructurePersistentItem.Columns.LESSON, persistentObject.task.lesson)
        put(DBStructurePersistentItem.Columns.STEP, persistentObject.task.step)
    }

    override fun parsePersistentObject(cursor: Cursor) = PersistentItem(
            localFileName = cursor.getString(cursor.getColumnIndex(DBStructurePersistentItem.Columns.LOCAL_FILE_NAME)),
            localFileDir  = cursor.getString(cursor.getColumnIndex(DBStructurePersistentItem.Columns.LOCAL_FILE_DIR)),
            isInAppInternalDir = cursor.getBoolean(DBStructurePersistentItem.Columns.IS_IN_APP_INTERNAL_DIR),
            downloadId   = cursor.getLong(cursor.getColumnIndex(DBStructurePersistentItem.Columns.DOWNLOAD_ID)),

            status       = PersistentItem.Status.valueOf(cursor.getString(cursor.getColumnIndex(DBStructurePersistentItem.Columns.STATUS))),

            task = DownloadTask(
                    originalPath  = cursor.getString(cursor.getColumnIndex(DBStructurePersistentItem.Columns.ORIGINAL_PATH)),
                    course       = cursor.getLong(cursor.getColumnIndex(DBStructurePersistentItem.Columns.COURSE)),
                    section      = cursor.getLong(cursor.getColumnIndex(DBStructurePersistentItem.Columns.SECTION)),
                    unit         = cursor.getLong(cursor.getColumnIndex(DBStructurePersistentItem.Columns.UNIT)),
                    lesson       = cursor.getLong(cursor.getColumnIndex(DBStructurePersistentItem.Columns.LESSON)),
                    step         = cursor.getLong(cursor.getColumnIndex(DBStructurePersistentItem.Columns.STEP))
            )
    )

    override fun getItems(selector: Map<String, String>): Observable<List<PersistentItem>> =
            Observable.fromCallable { if (selector.isEmpty()) getAll() else getAll(selector) }

    override fun getItem(selector: Map<String, String>): Maybe<PersistentItem> = Maybe.create { emitter ->
        get(selector)?.let(emitter::onSuccess) ?: emitter.onComplete()
    }
}