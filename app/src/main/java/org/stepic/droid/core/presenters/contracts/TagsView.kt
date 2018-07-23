package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.Tag


interface TagsView {
    fun onTagsFetched(tags: List<Tag>)

    fun onTagsNotLoaded()
}