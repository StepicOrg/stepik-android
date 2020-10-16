package org.stepik.android.cache.progress

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.progress.source.ProgressCacheDataSource
import org.stepik.android.model.Progress
import javax.inject.Inject

class ProgressCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : ProgressCacheDataSource {
    override fun getProgresses(progressIds: List<String>): Single<List<Progress>> =
        Single.fromCallable {
            databaseFacade.getProgresses(progressIds)
        }

    override fun saveProgresses(progresses: List<Progress>): Completable =
        Completable.fromAction {
            databaseFacade.addProgresses(progresses)
        }
}