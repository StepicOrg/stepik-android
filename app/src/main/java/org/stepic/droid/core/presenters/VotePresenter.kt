package org.stepic.droid.core.presenters

import android.support.annotation.WorkerThread
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.VoteView
import org.stepic.droid.di.comment.CommentsScope
import org.stepik.android.model.comments.Vote
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CommentsScope
class VotePresenter
@Inject constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val api: Api
) : PresenterBase<VoteView>() {

    fun doVote(vote: Vote, commentId: Long) {
        threadPoolExecutor.execute {
            try {
                val isSuccessful = api.makeVote(vote.id, vote.value).execute().isSuccessful
                if (isSuccessful) {
                    onSuccess(commentId)
                } else {
                    onFail()
                }
            } catch (exception: Exception) {
                onFail()
            }
        }

//        api.makeVote(it, voteValue).enqueue(object : Callback<VoteResponse> {
//            override fun onResponse(call: Call<VoteResponse>?, response: Response<VoteResponse>?) {
//                //todo event for update
//                if (response?.isSuccessful ?: false) {
//                    bus.post(LikeCommentSuccessEvent(commentId, voteObject))
//                } else {
//                    bus.post(LikeCommentFailEvent())
//                }
//            }
//
//            override fun onFailure(call: Call<VoteResponse>?, t: Throwable?) {
//                //todo event for fail
//                bus.post(LikeCommentFailEvent())
//            }
//        })
    }

    @WorkerThread
    private fun onSuccess(commentId: Long) {
        mainHandler.post {
            view?.onVoteSuccess(commentId)
        }
    }

    @WorkerThread
    private fun onFail() {
        mainHandler.post {
            view?.onVoteFail()
        }
    }

}
