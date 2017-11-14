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
    override fun getDbName(): String = DbStructureSearchQuery.SEARCH_QUERY

    override fun getDefaultPrimaryColumn(): String = DbStructureSearchQuery.Column.QUERY_HASH

    override fun getDefaultPrimaryValue(persistentObject: SearchQuery): String = persistentObject.text.toLowerCase().hashCode().toString()

    override fun getContentValues(persistentObject: SearchQuery): ContentValues {
        val contentValues = ContentValues()

        contentValues.put(DbStructureSearchQuery.Column.QUERY_HASH, persistentObject.text.toLowerCase().hashCode())  // toLowerCase to avoid problems with case sensitive duplicates due to SQLite
        contentValues.put(DbStructureSearchQuery.Column.QUERY_TEXT, persistentObject.text)

        return contentValues
    }

    override fun parsePersistentObject(cursor: Cursor): SearchQuery =
            SearchQuery(
                    text = cursor.getString(cursor.getColumnIndex(DbStructureSearchQuery.Column.QUERY_TEXT)),
                    source = SearchQuerySource.DB
            )

    override fun getSearchQueries(constraint: String, count: Int): List<SearchQuery> {
        val sql =
                "SELECT * FROM $dbName " +
                "WHERE ${DbStructureSearchQuery.Column.QUERY_TEXT} LIKE ? " +
                        "ORDER BY ${DbStructureSearchQuery.Column.QUERY_TIMESTAMP} DESC " +
                        "LIMIT $count"

        val pattern = "%${constraint.toLowerCase()}%"
        return getAllWithQuery(sql, arrayOf(pattern))
    }

}