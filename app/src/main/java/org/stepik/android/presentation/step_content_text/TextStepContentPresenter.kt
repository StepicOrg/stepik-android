package org.stepik.android.presentation.step_content_text

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.step_content_text.interactor.TextContentFontInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class TextStepContentPresenter
@Inject
constructor(
    private val textContentFontInteractor: TextContentFontInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<TextStepContentView>() {
    fun onSetTextContentSize() {
        compositeDisposable += textContentFontInteractor
            .fetchTextContentFontSize()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { view?.setTextContentFontSize(it) },
                onError = emptyOnErrorStub
            )
    }
}