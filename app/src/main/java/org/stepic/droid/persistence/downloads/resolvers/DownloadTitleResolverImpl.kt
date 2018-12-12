package org.stepic.droid.persistence.downloads.resolvers

import io.reactivex.Single
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepik.android.domain.lesson.repository.LessonRepository
import javax.inject.Inject

@PersistenceScope
class DownloadTitleResolverImpl
@Inject
constructor(
        private val lessonRepository: LessonRepository
) : DownloadTitleResolver {
    override fun resolveTitle(lessonId: Long, stepId: Long): Single<String> =
        lessonRepository
            .getLesson(lessonId)
            .map {
                "${it.title} - ${it.steps.indexOf(stepId) + 1}"
            }
            .toSingle()
}