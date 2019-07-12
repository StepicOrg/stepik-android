package org.stepic.droid.di.analytic

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.AnalyticImpl
import org.stepic.droid.analytic.experiments.AchievementsSplitTest
import org.stepic.droid.analytic.experiments.CommentsTooltipSplitTest
import org.stepic.droid.analytic.experiments.SplitTest
import org.stepic.droid.di.AppSingleton

@Module
abstract class AnalyticModule {

    @AppSingleton
    @Binds
    internal abstract fun bindAnalytic(analyticImpl: AnalyticImpl): Analytic

    @AppSingleton
    @Binds
    @IntoSet
    internal abstract fun bindAchievementsSplitTest(achievementsSplitTest: AchievementsSplitTest): SplitTest<*>

    @AppSingleton
    @Binds
    @IntoSet
    internal abstract fun bindCommentsSplitTest(commentsTooltipSplitTest: CommentsTooltipSplitTest): SplitTest<*>
}