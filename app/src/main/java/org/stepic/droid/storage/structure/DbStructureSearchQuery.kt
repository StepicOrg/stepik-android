package org.stepic.droid.storage.structure

object DbStructureSearchQuery {
    const val SEARCH_QUERY = "search_query"

    object Column {
        const val QUERY_TEXT = "query_text"
        const val QUERY_TIMESTAMP = "query_timestamp"
    }

    const val TABLE_SIZE_LIMIT = 100
    const val LIMITER_TRIGGER_NAME = "search_query_size_limiter_trigger"
}