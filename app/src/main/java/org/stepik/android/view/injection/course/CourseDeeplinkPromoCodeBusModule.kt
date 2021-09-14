package org.stepik.android.view.injection.course

import com.jakewharton.rxrelay2.BehaviorRelay
import dagger.Module
import dagger.Provides
import org.stepic.droid.di.AppSingleton
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode

@Module
object CourseDeeplinkPromoCodeBusModule {
    @Provides
    @JvmStatic
    @AppSingleton
    internal fun provideDeeplinkPromoCodeBehaviorRelay(): BehaviorRelay<DeeplinkPromoCode> =
        BehaviorRelay.createDefault(DeeplinkPromoCode.EMPTY)
}