package org.stepik.android.remote.discussion_thread

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.StepicRestLoggedService
import org.stepik.android.data.discussion_thread.source.DiscussionThreadRemoteDataSource
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.discussion_thread.model.DiscussionThreadResponse
import javax.inject.Inject

class DiscussionThreadRemoteDataSourceImpl
@Inject
constructor(
    private val stepicRestLoggedService: StepicRestLoggedService
) : DiscussionThreadRemoteDataSource {
    private val mapper =
        Function<DiscussionThreadResponse, List<DiscussionThread>>(DiscussionThreadResponse::discussionThreads)

    override fun getDiscussionThreads(vararg ids: String): Single<List<DiscussionThread>> =
        ids.chunkedSingleMap { threadIds ->
            stepicRestLoggedService
                .getDiscussionThreads(threadIds)
                .map(mapper)
        }
}