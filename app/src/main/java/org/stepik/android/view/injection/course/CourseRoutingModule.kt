package org.stepik.android.view.injection.course

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.stepik.android.view.course.routing.CourseBranchDeepLinkParser
import org.stepik.android.view.course.routing.CourseBranchDeepLinkRouter
import org.stepik.android.view.routing.deeplink.BranchDeepLinkParser
import org.stepik.android.view.routing.deeplink.BranchDeepLinkRouter

@Module
abstract class CourseRoutingModule {
    @Binds
    @IntoSet
    internal abstract fun bindCourseBranchDeepLinkParser(
        courseBranchDeepLinkParser: CourseBranchDeepLinkParser
    ): BranchDeepLinkParser

    @Binds
    @IntoSet
    internal abstract fun bindCourseBranchDeepLinkRouter(
        courseBranchDeepLinkRouter: CourseBranchDeepLinkRouter
    ): BranchDeepLinkRouter
}