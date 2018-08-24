package org.stepic.droid.storage.repositories.progress

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.web.Api
import org.stepik.android.model.Progressable
import javax.inject.Inject

class ProgressRepositoryImpl
@Inject
constructor(
        private val api: Api,
        private val databaseFacade: DatabaseFacade
) : ProgressRepository {
    override fun syncProgresses(vararg progressables: Progressable): Completable =
            Single.fromCallable {
                api.getProgresses(progressables.mapNotNull(Progressable::progress).toTypedArray()).execute().body()?.progresses!!
            }.doOnSuccess { progresses ->
                progresses.forEach(databaseFacade::addProgress)
            }.toCompletable()
}