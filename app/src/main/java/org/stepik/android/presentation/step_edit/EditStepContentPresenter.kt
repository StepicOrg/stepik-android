package org.stepik.android.presentation.step_edit

import io.reactivex.Scheduler
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class EditStepContentPresenter
@Inject
constructor(
    private val analytic: Analytic,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<EditStepContentView>() {

}