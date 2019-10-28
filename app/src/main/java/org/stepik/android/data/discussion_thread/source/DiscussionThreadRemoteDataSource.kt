package org.stepik.android.data.discussion_thread.source

import io.reactivex.Single
import org.stepik.android.model.comments.DiscussionThread

interface DiscussionThreadRemoteDataSource {
    fun getDiscussionThreads(vararg ids: String): Single<List<DiscussionThread>>
}