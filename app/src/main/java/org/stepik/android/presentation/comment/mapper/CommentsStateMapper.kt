package org.stepik.android.presentation.comment.mapper

import org.stepic.droid.util.PagedList
import org.stepic.droid.util.plus
import org.stepik.android.domain.comment.interactor.CommentInteractor
import org.stepik.android.presentation.comment.CommentsView
import org.stepik.android.presentation.comment.model.CommentItem
import javax.inject.Inject

class CommentsStateMapper
@Inject
constructor() {
    fun mapToLoadMoreState(commentsState: CommentsView.CommentsState.Loaded, direction: CommentInteractor.Direction): CommentsView.CommentsState =
        when (direction) {
            CommentInteractor.Direction.UP ->
                commentsState.copy(commentItems = listOf(CommentItem.Placeholder) + commentsState.commentItems)

            CommentInteractor.Direction.DOWN ->
                commentsState.copy(commentItems = commentsState.commentItems + CommentItem.Placeholder)
        }

    fun mapFromLoadMoreToSuccess(state: CommentsView.State, items: PagedList<CommentItem.Data>, direction: CommentInteractor.Direction): CommentsView.State {
        if (state !is CommentsView.State.DiscussionLoaded ||
            state.commentsState !is CommentsView.CommentsState.Loaded) {
            return state
        }

        val commentsState = state.commentsState

        val (newDataItems: PagedList<CommentItem.Data>, newItems) =
            when (direction) {
                CommentInteractor.Direction.UP ->
                    items + commentsState.commentDataItems to items + commentsState.commentItems.dropWhile(CommentItem.Placeholder::equals)

                CommentInteractor.Direction.DOWN ->
                    commentsState.commentDataItems + items to commentsState.commentItems.dropLastWhile(CommentItem.Placeholder::equals) + items
            }

        return state.copy(commentsState = commentsState.copy(commentDataItems = newDataItems, commentItems = newItems))
    }

    fun mapFromLoadMoreToError(state: CommentsView.State, direction: CommentInteractor.Direction): CommentsView.State {
        if (state !is CommentsView.State.DiscussionLoaded ||
            state.commentsState !is CommentsView.CommentsState.Loaded) {
            return state
        }

        val newItems =
            when (direction) {
                CommentInteractor.Direction.UP ->
                   state.commentsState.commentItems.dropWhile(CommentItem.Placeholder::equals)

                CommentInteractor.Direction.DOWN ->
                   state.commentsState.commentItems.dropLastWhile(CommentItem.Placeholder::equals)
            }

        return state.copy(commentsState = state.commentsState.copy(commentItems = newItems))
    }
}