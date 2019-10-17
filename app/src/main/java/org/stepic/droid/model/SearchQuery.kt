package org.stepic.droid.model

import androidx.annotation.DrawableRes
import org.stepic.droid.R

enum class SearchQuerySource(@DrawableRes val iconRes: Int) {
    DB(R.drawable.ic_history),
    API(R.drawable.ic_action_search)
}

class SearchQuery
@JvmOverloads // required to generate empty constructor that will be called by Gson on deserialization and init source field with default value instead of null
constructor(
        val text: String = "",
        val source: SearchQuerySource = SearchQuerySource.API)