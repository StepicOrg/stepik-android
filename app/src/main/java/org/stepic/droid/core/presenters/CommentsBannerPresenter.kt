package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.core.presenters.contracts.CommentsView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.comments.interactor.CommentsInteractor
import timber.log.Timber
import javax.inject.Inject

class CommentsBannerPresenter
@Inject
constructor(
    private val commentsBannerInteractor: CommentsInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CommentsView>() {


    private val commentsDisposable = CompositeDisposable()

    fun fetchCommentsBanner(courseId: Long) {
        commentsDisposable += commentsBannerInteractor
            .shouldShowCommentsBannerForCourse(courseId)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { wasCommentsBannerShown ->
                    if (!wasCommentsBannerShown) {
                        view?.showCommentsBanner()
                    }
                },
                onError = { Timber.d(it) }
            )
    }
}