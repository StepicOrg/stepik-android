package org.stepik.android.view.catalog.mapper

import android.content.Context
import org.stepic.droid.R
import javax.inject.Inject

class AuthorCountMapper
@Inject
constructor() {
    companion object {
        private const val MAX_AUTHOR_COUNT = 99
    }

    fun mapAuthorCountToString(context: Context, count: Int): String =
        if (count > MAX_AUTHOR_COUNT) {
            context.resources.getString(R.string.author_max_count)
        } else {
            context.resources.getQuantityString(R.plurals.catalog_author_lists, count, count)
        }
}