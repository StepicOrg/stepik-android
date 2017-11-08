package org.stepic.droid.model

enum class SearchQuerySource {
    DB, API
}

data class SearchQuery(
        val text: String,
        val source: SearchQuerySource = SearchQuerySource.API)