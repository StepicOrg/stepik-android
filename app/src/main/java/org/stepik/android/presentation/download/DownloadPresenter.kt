package org.stepik.android.presentation.download

import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.download.interactor.DownloadInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class DownloadPresenter
@Inject
constructor(
    private val downloadInteractor: DownloadInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<DownloadView>() {

}