package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.model.SearchQuery
import org.stepic.droid.model.SearchQuerySource
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureSearchQuery
import javax.inject.Inject


class SearchQueryDaoImpl @Inject
constructor(databaseOperations: DatabaseOperations) : DaoBase<SearchQuery>(databaseOperations), SearchQueryDao {
    companion object {
        private const val COURSE_PREFIX = "course_id"
    }
    override fun getDbName(): String = DbStructureSearchQuery.SEARCH_QUERY

    override fun getDefaultPrimaryColumn(): String = DbStructureSearchQuery.Column.QUERY_HASH

    override fun getDefaultPrimaryValue(persistentObject: SearchQuery): String = persistentObject.text.toLowerCase().hashCode().toString()

    override fun getContentValues(persistentObject: SearchQuery): ContentValues {
        val contentValues = ContentValues()

        contentValues.put(DbStructureSearchQuery.Column.QUERY_COURSE_ID, persistentObject.courseId)
        val queryHash = if (persistentObject.courseId != -1L) {
            "${COURSE_PREFIX}_${persistentObject.courseId}_${persistentObject.text}".toLowerCase().hashCode()
        } else {
            persistentObject.text.toLowerCase().hashCode()
        }
        contentValues.put(DbStructureSearchQuery.Column.QUERY_HASH, queryHash)  // toLowerCase to avoid problems with case sensitive duplicates due to SQLite
        contentValues.put(DbStructureSearchQuery.Column.QUERY_TEXT, persistentObject.text)

        return contentValues
    }

    override fun parsePersistentObject(cursor: Cursor): SearchQuery =
            SearchQuery(
                    text = cursor.getString(cursor.getColumnIndex(DbStructureSearchQuery.Column.QUERY_TEXT)),
                    source = SearchQuerySource.DB
            )

    override fun getSearchQueries(courseId: Long, constraint: String, count: Int): List<SearchQuery> {
        val sql =
                "SELECT * FROM $dbName " +
                "WHERE ${DbStructureSearchQuery.Column.QUERY_TEXT} LIKE ? AND ${DbStructureSearchQuery.Column.QUERY_COURSE_ID} = ?" +
                        "ORDER BY ${DbStructureSearchQuery.Column.QUERY_TIMESTAMP} DESC " +
                        "LIMIT $count"

        val pattern = "%${constraint.toLowerCase()}%"
        return getAllWithQuery(sql, arrayOf(pattern, courseId.toString()))
    }

}