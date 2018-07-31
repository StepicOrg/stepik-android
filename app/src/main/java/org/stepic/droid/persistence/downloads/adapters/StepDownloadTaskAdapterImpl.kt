package org.stepic.droid.persistence.downloads.adapters

import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.content.StepContentResolver
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.DownloadTask
import org.stepic.droid.storage.repositories.Repository
import org.stepik.android.model.Step
import javax.inject.Inject

@PersistenceScope
class StepDownloadTaskAdapterImpl
@Inject
constructor(
        private val stepRepository: Repository<Step>,
        private val stepContentResolver: StepContentResolver
): StepDownloadTaskAdapter {
    override fun convertToTask(
            courseId: Long,
            sectionId: Long,
            unitId: Long,
            lessonId: Long,
            vararg stepIds: Long,
            configuration: DownloadConfiguration
    ): Observable<DownloadTask> = Observable
            .just(stepIds)
            .map(stepRepository::getObjects)
            .flatMap(Iterable<Step>::toObservable)
            .map { it to stepContentResolver.getDownloadableContentFromStep(it, configuration) }
            .flatMap { (step, paths) ->
                paths.map { path ->
                    DownloadTask(
                            course = courseId,
                            section = sectionId,
                            unit = unitId,
                            lesson = lessonId,
                            step = step.id,
                            originalPath = path
                    )
                }.toObservable()
            }
}