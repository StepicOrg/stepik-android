package org.stepik.android.cache.progress

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.progress.source.ProgressCacheDataSource
import org.stepik.android.model.Progress
import javax.inject.Inject

class ProgressCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : ProgressCacheDataSource {
    override fun getProgress(progressId: String): Maybe<Progress> =
        Maybe.create { emitter ->
            databaseFacade.getProgressById(progressId)?.let(emitter::onSuccess) ?: emitter.onComplete()
        }

    override fun saveProgress(progress: Progress): Completable =
        Completable.fromAction {
            databaseFacade.addProgress(progress)
        }
}