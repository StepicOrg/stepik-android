package org.stepik.android.presentation.font_size_settings

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.step_content_text.interactor.TextContentFontInteractor
import org.stepik.android.domain.step_content_text.model.FontSize
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class FontSizePresenter
@Inject
constructor(
    private val textContentFontInteractor: TextContentFontInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<FontSizeView>() {
    fun fetchFontSize() {
        compositeDisposable += textContentFontInteractor
            .fetchTextContentFontSize()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { view?.setCachedFontSize(it) },
                onError = emptyOnErrorStub
            )
    }

    fun onFontSizeChosen(fontSize: FontSize) {
        compositeDisposable += textContentFontInteractor
            .setTextContentFontSize(fontSize)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onError = emptyOnErrorStub
            )
    }
}