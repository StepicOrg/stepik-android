package org.stepik.android.domain.progress.repository

import io.reactivex.Single
import org.stepik.android.model.Progress

interface ProgressRepository {
    fun getProgress(progressId: String): Single<Progress>
}