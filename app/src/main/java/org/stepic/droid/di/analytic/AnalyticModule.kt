package org.stepic.droid.di.analytic

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.AnalyticImpl
import org.stepic.droid.analytic.StepikAnalytic
import org.stepic.droid.analytic.StepikAnalyticImpl
import org.stepic.droid.analytic.experiments.CoursePurchaseWebviewSplitTest
import org.stepic.droid.analytic.experiments.DeferredAuthSplitTest
import org.stepic.droid.analytic.experiments.InAppPurchaseSplitTest
import org.stepic.droid.analytic.experiments.SplitTest
import org.stepic.droid.di.AppSingleton

@Module
abstract class AnalyticModule {

    @AppSingleton
    @Binds
    internal abstract fun bindAnalytic(analyticImpl: AnalyticImpl): Analytic

    @AppSingleton
    @Binds
    internal abstract fun bindStepikAnalytic(stepikAnalyticImpl: StepikAnalyticImpl): StepikAnalytic

    @AppSingleton
    @Binds
    @IntoSet
    internal abstract fun bindDeferredAuthSplitTest(deferredAuthSplitTest: DeferredAuthSplitTest): SplitTest<*>

    @AppSingleton
    @Binds
    @IntoSet
    internal abstract fun bindCoursePurchaseWebviewSplitTest(coursePurchaseWebviewSplitTest: CoursePurchaseWebviewSplitTest): SplitTest<*>

    @AppSingleton
    @Binds
    @IntoSet
    internal abstract fun bindInAppPurchaseSplitTest(inAppPurchaseSplitTest: InAppPurchaseSplitTest): SplitTest<*>
}