package org.stepic.droid.di.course_general

import dagger.Binds
import dagger.Module
import org.stepic.droid.base.Client
import org.stepic.droid.base.ClientImpl
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.base.ListenerContainerImpl
import org.stepic.droid.core.dropping.DroppingPosterImpl
import org.stepic.droid.core.dropping.contract.DroppingListener
import org.stepic.droid.core.dropping.contract.DroppingPoster
import org.stepic.droid.core.joining.JoiningPosterImpl
import org.stepic.droid.core.joining.contract.JoiningListener
import org.stepic.droid.core.joining.contract.JoiningPoster
import org.stepic.droid.di.course_list.CourseGeneralScope

@Module
interface CourseGeneralModule {

    @Binds
    @CourseGeneralScope
    fun bindsClient(clientImpl: ClientImpl<DroppingListener>): Client<DroppingListener>

    @Binds
    @CourseGeneralScope
    fun bindContainer(listenerContainer: ListenerContainerImpl<DroppingListener>): ListenerContainer<DroppingListener>

    @Binds
    @CourseGeneralScope
    fun bindsPoster(posterImpl: DroppingPosterImpl): DroppingPoster


    @Binds
    @CourseGeneralScope
    fun bindsJoinClient(clientImpl: ClientImpl<JoiningListener>): Client<JoiningListener>

    @Binds
    @CourseGeneralScope
    fun bindJoinContainer(listenerContainer: ListenerContainerImpl<JoiningListener>): ListenerContainer<JoiningListener>

    @Binds
    @CourseGeneralScope
    fun bindsJoinPoster(posterImpl: JoiningPosterImpl): JoiningPoster

}
