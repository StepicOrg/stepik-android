package org.stepic.droid.persistence.downloads.resolvers

import io.reactivex.Single
import org.stepic.droid.persistence.model.Structure

interface DownloadTitleResolver {
    fun resolveTitle(lessonId: Long, stepId: Long): Single<String>

    fun resolveTitle(structure: Structure): Single<String> =
            resolveTitle(structure.lesson, structure.step)
}