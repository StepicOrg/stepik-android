package org.stepik.android.domain.step.interactor

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.network.repository.NetworkTypeRepository
import org.stepik.android.domain.progress.interactor.LocalProgressInteractor
import org.stepik.android.domain.view_assignment.repository.ViewAssignmentRepository
import org.stepik.android.model.ViewAssignment
import javax.inject.Inject

class StepDeferableViewReportInteractor
@Inject
constructor(
    private val networkTypeRepository: NetworkTypeRepository,
    private val viewAssignmentRepository: ViewAssignmentRepository,
    private val localProgressInteractor: LocalProgressInteractor
) {

    fun reportStepsViewsOnActiveNetwork(): Completable =
        waitUntilActiveNetwork()
            .andThen(viewAssignmentRepository.getViewAssignments())
            .flatMapCompletable { viewAssignments ->
                viewAssignments
                    .toObservable()
                    .concatMap(::reportStepViewFromQueue)
                    .toList()
                    .flatMapCompletable { steps ->
                        localProgressInteractor
                            .updateStepsProgress(*steps.toLongArray())
                    }
            }

    private fun waitUntilActiveNetwork(): Completable =
        networkTypeRepository
            .getAvailableNetworkTypesStream()
            .filter { it.isNotEmpty() }
            .take(1)
            .ignoreElements()

    private fun reportStepViewFromQueue(viewAssignment: ViewAssignment): Observable<Long> =
        viewAssignmentRepository
            .createViewAssignment(viewAssignment, dataSourceType = DataSourceType.REMOTE)
            .andThen(viewAssignmentRepository.removeViewAssignment(viewAssignment))
            .andThen(Observable.just(viewAssignment.step))
            .onErrorResumeNext(Observable.empty())

}