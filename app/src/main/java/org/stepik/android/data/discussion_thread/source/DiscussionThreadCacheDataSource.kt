package org.stepik.android.data.discussion_thread.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.comments.DiscussionThread

interface DiscussionThreadCacheDataSource {
    fun getDiscussionThreads(vararg ids: String): Single<List<DiscussionThread>>
    fun saveDiscussionThreads(discussionThreads: List<DiscussionThread>): Completable
}