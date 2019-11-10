package org.stepik.android.remote.progress.service

import io.reactivex.Single
import org.stepik.android.remote.progress.model.ProgressResponse

interface ProgressService {
    fun getProgressesReactive(progresses: Array<String?>): Single<ProgressResponse>
}