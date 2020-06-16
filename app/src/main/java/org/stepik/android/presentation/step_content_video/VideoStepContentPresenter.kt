package org.stepik.android.presentation.step_content_video

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.model.StepPersistentWrapper
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepik.android.domain.step_content_video.interactor.VideoLengthInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class VideoStepContentPresenter
@Inject
constructor(
    private val videoLengthInteractor: VideoLengthInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<VideoStepContentView>() {
    private var state: VideoStepContentView.State = VideoStepContentView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: VideoStepContentView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchVideoLength(stepWrapper: StepPersistentWrapper) {
        if (state != VideoStepContentView.State.Idle) return

        state = VideoStepContentView.State.Loading
        compositeDisposable += videoLengthInteractor
            .getVideoLengthFormatted(stepWrapper)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = { state = VideoStepContentView.State.Loaded(videoLength = null) },
                onSuccess = { state = VideoStepContentView.State.Loaded(it) },
                onError = emptyOnErrorStub
            )
    }
}