package org.stepik.android.domain.view_assignment.interactor

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.BehaviorSubject
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.network.repository.NetworkTypeRepository
import org.stepik.android.domain.progress.interactor.LocalProgressInteractor
import org.stepik.android.domain.view_assignment.repository.ViewAssignmentRepository
import org.stepik.android.model.ViewAssignment
import org.stepik.android.view.injection.view_assignment.ViewAssignmentBus
import javax.inject.Inject

class DeferrableViewAssignmentReportInteractor
@Inject
constructor(
    private val networkTypeRepository: NetworkTypeRepository,
    private val viewAssignmentRepository: ViewAssignmentRepository,
    private val localProgressInteractor: LocalProgressInteractor,

    @ViewAssignmentBus
    private val viewAssignmentObserver: BehaviorSubject<Unit>,

    @ViewAssignmentBus
    private val viewAssignmentObservable: Observable<Unit>
) {
    fun reportViewAssignmentsOnDemand(): Completable =
        viewAssignmentObservable
            .concatMapCompletable {
                reportViewAssignmentsOnActiveNetwork()
                    .doOnSuccess { isSuccess ->
                        if (!isSuccess) {
                            viewAssignmentObserver.onNext(Unit)
                        }
                    }
                    .ignoreElement()
            }

    /**
     * Returns true if all view assignments from queue were successfully posted
     */
    private fun reportViewAssignmentsOnActiveNetwork(): Single<Boolean> =
        waitUntilActiveNetwork()
            .andThen(viewAssignmentRepository.getViewAssignments())
            .flatMap { viewAssignments ->
                viewAssignments
                    .toObservable()
                    .concatMap(::reportViewAssignmentFromQueue)
                    .toList()
                    .flatMap { steps ->
                        localProgressInteractor
                            .updateStepsProgress(*steps.toLongArray())
                            .toSingleDefault(viewAssignments.size == steps.size)
                    }
            }
            .onErrorReturnItem(false)

    private fun waitUntilActiveNetwork(): Completable =
        networkTypeRepository
            .getAvailableNetworkTypesStream()
            .filter { it.isNotEmpty() }
            .take(1)
            .ignoreElements()

    private fun reportViewAssignmentFromQueue(viewAssignment: ViewAssignment): Observable<Long> =
        viewAssignmentRepository
            .createViewAssignment(viewAssignment, dataSourceType = DataSourceType.REMOTE)
            .andThen(viewAssignmentRepository.removeViewAssignment(viewAssignment))
            .andThen(Observable.just(viewAssignment.step))
            .onErrorResumeNext(Observable.empty())
}