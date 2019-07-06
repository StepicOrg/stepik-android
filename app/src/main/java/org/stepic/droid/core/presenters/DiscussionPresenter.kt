package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.core.presenters.contracts.DiscussionView
import org.stepic.droid.di.comment.CommentsScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.discussion_proxy.interactor.DiscussionProxyInteractor
import javax.inject.Inject

@CommentsScope
class DiscussionPresenter
@Inject
constructor(
    private val discussionProxyInteractor: DiscussionProxyInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<DiscussionView>() {
    private val compositeDisposable = CompositeDisposable()

    fun loadDiscussion(discussionId: String) {
        compositeDisposable.clear()
        compositeDisposable += discussionProxyInteractor
            .getDiscussionProxy(discussionId)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { discussionProxy ->
                    if (discussionProxy.discussions.isEmpty()) {
                        view?.onEmptyComments(discussionProxy)
                    } else {
                        view?.onLoaded(discussionProxy)
                    }
                },
                onError = { view?.onInternetProblemInComments() }
            )
    }
}
