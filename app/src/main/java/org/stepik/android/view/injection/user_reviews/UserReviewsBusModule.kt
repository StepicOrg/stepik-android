package org.stepik.android.view.injection.user_reviews

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepik.android.domain.user_reviews.model.UserCourseReviewOperation

@Module
object UserReviewsBusModule {
    @Provides
    @JvmStatic
    @AppSingleton
    @UserCourseReviewOperationBus
    internal fun provideUserCourseOperationPublisher(): PublishSubject<UserCourseReviewOperation> =
        PublishSubject.create()

    @Provides
    @JvmStatic
    @AppSingleton
    @UserCourseReviewOperationBus
    internal fun provideUserCourseOperationObservable(
        @UserCourseReviewOperationBus
        userCourseOperationPublisher: PublishSubject<UserCourseReviewOperation>,
        @BackgroundScheduler
        scheduler: Scheduler
    ): Observable<UserCourseReviewOperation> =
        userCourseOperationPublisher.observeOn(scheduler)
}