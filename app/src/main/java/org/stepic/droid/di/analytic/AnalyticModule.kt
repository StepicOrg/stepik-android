package org.stepic.droid.di.analytic

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.AnalyticImpl
import org.stepic.droid.analytic.experiments.AchievementsSplitTest
import org.stepic.droid.analytic.experiments.CommentsSplitTest
import org.stepic.droid.analytic.experiments.SplitTest
import org.stepic.droid.analytic.experiments.PersonalDeadlinesSplitTest
import org.stepic.droid.analytic.experiments.VideoSplitTest
import org.stepic.droid.di.AppSingleton

@Module
abstract class AnalyticModule {

    @AppSingleton
    @Binds
    internal abstract fun bindAnalytic(analyticImpl: AnalyticImpl): Analytic

    @Binds
    @IntoSet
    internal abstract fun bindPersonalDeadlinesSplitTest(personalDeadlinesSplitTest: PersonalDeadlinesSplitTest) : SplitTest<*>

    @AppSingleton
    @Binds
    @IntoSet
    internal abstract fun bindAchievementsSplitTest(achievementsSplitTest: AchievementsSplitTest): SplitTest<*>

    @AppSingleton
    @Binds
    @IntoSet
    internal abstract fun bindCommentsSplitTest(commentsSplitTest: CommentsSplitTest): SplitTest<*>

    @AppSingleton
    @Binds
    @IntoSet
    internal abstract fun bindVideoSplitTest(videoSplitTest: VideoSplitTest): SplitTest<*>
}