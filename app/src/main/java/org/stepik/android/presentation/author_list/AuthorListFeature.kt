package org.stepik.android.presentation.author_list

import org.stepik.android.domain.catalog_block.model.AuthorCatalogBlockContentItem
import org.stepik.android.domain.catalog_block.model.CatalogBlockContent

interface AuthorListFeature {
    sealed class State {
        object Idle : State()
        data class Content(val authorListItems: List<AuthorCatalogBlockContentItem>) : State()
    }

    sealed class Message {
        data class InitMessage(val id: Long, val authorList: CatalogBlockContent.AuthorCourseList) : Message()
    }

    sealed class Action {
        sealed class ViewAction : Action()
    }
}