package org.stepik.android.cache.discussion_thread

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.discussion_thread.structure.DbStructureDiscussionThread
import org.stepik.android.data.discussion_thread.source.DiscussionThreadCacheDataSource
import org.stepik.android.model.comments.DiscussionThread
import javax.inject.Inject

class DiscussionThreadCacheDataSourceImpl
@Inject
constructor(
    private val discussionThreadDao: IDao<DiscussionThread>
) : DiscussionThreadCacheDataSource {
    override fun getDiscussionThreads(vararg ids: String): Single<List<DiscussionThread>> =
        Single.fromCallable {
            discussionThreadDao
                .getAllInRange(DbStructureDiscussionThread.Columns.ID, ids.joinToString())
        }

    override fun saveDiscussionThreads(discussionThreads: List<DiscussionThread>): Completable =
        Completable.fromAction {
            discussionThreadDao.insertOrReplaceAll(discussionThreads)
        }
}