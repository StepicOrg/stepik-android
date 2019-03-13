package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.CommentsView
import org.stepik.android.domain.comments.interactor.CommentsInteractor
import javax.inject.Inject

class CommentsBannerPresenter
@Inject
constructor(
    private val commentsBannerInteractor: CommentsInteractor
) : PresenterBase<CommentsView>() {}