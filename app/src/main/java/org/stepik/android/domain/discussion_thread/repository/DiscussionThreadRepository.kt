package org.stepik.android.domain.discussion_thread.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.comments.DiscussionThread

interface DiscussionThreadRepository {
    fun getDiscussionThreads(vararg ids: String, primarySourceType: DataSourceType = DataSourceType.CACHE): Single<List<DiscussionThread>>
}