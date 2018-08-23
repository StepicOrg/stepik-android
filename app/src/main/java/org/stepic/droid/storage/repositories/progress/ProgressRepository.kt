package org.stepic.droid.storage.repositories.progress

import io.reactivex.Completable
import org.stepik.android.model.Progressable

interface ProgressRepository {
    fun syncProgresses(vararg progressables: Progressable): Completable
}