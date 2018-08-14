package org.stepic.droid.persistence.downloads.resolvers

import io.reactivex.Single
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.storage.repositories.Repository
import org.stepik.android.model.Lesson
import javax.inject.Inject

@PersistenceScope
class DownloadTitleResolverImpl
@Inject
constructor(
        private val lessonRepository: Repository<Lesson>
) : DownloadTitleResolver {
    override fun resolveTitle(lessonId: Long, stepId: Long): Single<String> =
            Single.fromCallable { lessonRepository.getObject(lessonId) }.map {
                "${it.title} - ${it.steps.indexOf(stepId) + 1}"
            }
}