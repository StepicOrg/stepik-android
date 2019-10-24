package org.stepik.android.data.discussion_thread.repository

import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepic.droid.util.requireSize
import org.stepik.android.data.discussion_thread.source.DiscussionThreadCacheDataSource
import org.stepik.android.data.discussion_thread.source.DiscussionThreadRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.discussion_thread.repository.DiscussionThreadRepository
import org.stepik.android.model.comments.DiscussionThread
import javax.inject.Inject

class DiscussionThreadRepositoryImpl
@Inject
constructor(
     private val discussionThreadRemoteDataSource: DiscussionThreadRemoteDataSource,
     private val discussionThreadCacheDataSource: DiscussionThreadCacheDataSource
) : DiscussionThreadRepository {
    override fun getDiscussionThreads(vararg ids: String, primarySourceType: DataSourceType): Single<List<DiscussionThread>> {
        val remoteSource = discussionThreadRemoteDataSource
            .getDiscussionThreads(*ids)
            .doCompletableOnSuccess(discussionThreadCacheDataSource::saveDiscussionThreads)

        val cacheSource = discussionThreadCacheDataSource
            .getDiscussionThreads(*ids)

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource.requireSize(ids.size))

            DataSourceType.CACHE ->
                cacheSource.flatMap { cachedDiscussionThreads ->
                    val discussionThreadIds = (ids.toList() - cachedDiscussionThreads.map(DiscussionThread::id)).toTypedArray()
                    discussionThreadRemoteDataSource
                        .getDiscussionThreads(*discussionThreadIds)
                        .doCompletableOnSuccess(discussionThreadCacheDataSource::saveDiscussionThreads)
                        .map { remoteDiscussionThreads -> cachedDiscussionThreads + remoteDiscussionThreads }
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }.map { discussionThreads -> discussionThreads.sortedBy { ids.indexOf(it.id) } }
    }
}