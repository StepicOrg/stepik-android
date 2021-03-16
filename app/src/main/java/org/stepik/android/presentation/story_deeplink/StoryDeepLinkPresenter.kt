package org.stepik.android.presentation.story_deeplink

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.story_deeplink.interactor.StoryDeepLinkInteractor
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

class StoryDeepLinkPresenter
@Inject
constructor(
    private val storyDeepLinkInteractor: StoryDeepLinkInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<StoryDeepLinkView>() {
    private var state: StoryDeepLinkView.State = StoryDeepLinkView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    fun onData(storyId: Long) {
        if (state != StoryDeepLinkView.State.Idle) return

        state = StoryDeepLinkView.State.Loading
//        compositeDisposable += storyDeepLinkInteractor
//            .getStoryTemplate(storyId)
//            .observeOn(mainScheduler)
//            .subscribeOn(backgroundScheduler)
//            .subscribeBy(
//                onSuccess = { state = StoryDeepLinkView.State.Success(it) },
//                onError = { state = StoryDeepLinkView.State.Error }
//            )
    }
}