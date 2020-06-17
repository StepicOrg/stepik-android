package org.stepik.android.domain.view_assignment.service

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.base.App
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepik.android.domain.view_assignment.interactor.DeferrableViewAssignmentReportInteractor
import javax.inject.Inject

class DeferrableViewAssignmentReportService {
    @Inject
    lateinit var deferrableViewAssignmentReportInteractor: DeferrableViewAssignmentReportInteractor

    private val compositeDisposable = CompositeDisposable()

    init {
        /**
         * Motivation to inject in such way is to keep ViewAssignmentComponent
         * dependencies inside component and not expose them to whole app
         */
        App.component()
            .viewAssignmentComponentBuilder()
            .build()
            .inject(this)

        compositeDisposable += deferrableViewAssignmentReportInteractor
            .reportViewAssignmentsOnDemand()
            .subscribeBy(emptyOnErrorStub)
    }
}

class DeferrableViewAssignmentReportServiceContainer
@Inject
constructor() {
    private val service = DeferrableViewAssignmentReportService()
}