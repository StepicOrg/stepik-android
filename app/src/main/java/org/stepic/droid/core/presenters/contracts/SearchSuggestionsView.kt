package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.SearchQuery
import org.stepic.droid.model.SearchQuerySource

interface SearchSuggestionsView {
    fun setSuggestions(suggestions: List<SearchQuery>, source: SearchQuerySource)
}