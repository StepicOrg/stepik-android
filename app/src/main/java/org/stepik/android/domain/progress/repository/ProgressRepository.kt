package org.stepik.android.domain.progress.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Progress

interface ProgressRepository {
    fun getProgress(progressId: String): Single<Progress>
    fun getProgresses(vararg progressIds: String, primarySourceType: DataSourceType = DataSourceType.REMOTE): Single<List<Progress>>

    /**
     * Saves [progresses] to local storage
     */
    fun saveProgresses(progresses: List<Progress>): Completable
}