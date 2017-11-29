package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Tag

interface TagsView {
    fun onTagsFetched(tags: List<Tag>)

    fun onTagsNotLoaded()
}