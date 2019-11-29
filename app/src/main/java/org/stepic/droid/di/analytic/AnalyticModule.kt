package org.stepic.droid.di.analytic

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.AnalyticImpl
import org.stepic.droid.analytic.experiments.CatalogSearchSplitTest
import org.stepic.droid.analytic.experiments.SolutionStatsSplitTest
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
    internal abstract fun bindCatalogSearchSplitTest(catalogSearchSplitTest: CatalogSearchSplitTest): SplitTest<*>

    @AppSingleton
    @Binds
    @IntoSet
    internal abstract fun bindSolutionStatsSplitTest(solutionStatsSplitTest: SolutionStatsSplitTest): SplitTest<*>

}