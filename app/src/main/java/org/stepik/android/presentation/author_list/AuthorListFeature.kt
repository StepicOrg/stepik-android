package org.stepik.android.presentation.author_list

import org.stepik.android.domain.catalog_block.model.AuthorCatalogBlockContentItem
import org.stepik.android.domain.catalog_block.model.CatalogBlockContent
import ru.nobird.android.core.model.Identifiable

interface AuthorListFeature {
    sealed class State {
        object Idle : State()
        data class Content(val authorListItems: List<AuthorCatalogBlockContentItem>) : State()
    }

    sealed class Message : Identifiable<String> {
        data class InitMessage(override val id: String, val authorList: CatalogBlockContent.AuthorCourseList) : Message()
    }

    sealed class Action {
        sealed class ViewAction : Action()
    }
}