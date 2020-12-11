package org.stepik.android.presentation.author_list.dispatcher

import org.stepik.android.presentation.author_list.AuthorListFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class AuthorListActionDispatcher
@Inject
constructor() : RxActionDispatcher<AuthorListFeature.Action, AuthorListFeature.Message>() {
    override fun handleAction(action: AuthorListFeature.Action) {}
}