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
import org.stepic.droid.di.AppSingleton

@Module
interface CourseEnrollmentBusModule {

    @Binds
    @AppSingleton
    fun bindsClient(clientImpl: ClientImpl<DroppingListener>): Client<DroppingListener>

    @Binds
    @AppSingleton
    fun bindContainer(listenerContainer: ListenerContainerImpl<DroppingListener>): ListenerContainer<DroppingListener>

    @Binds
    @AppSingleton
    fun bindsPoster(posterImpl: DroppingPosterImpl): DroppingPoster


    @Binds
    @AppSingleton
    fun bindsJoinClient(clientImpl: ClientImpl<JoiningListener>): Client<JoiningListener>

    @Binds
    @AppSingleton
    fun bindJoinContainer(listenerContainer: ListenerContainerImpl<JoiningListener>): ListenerContainer<JoiningListener>

    @Binds
    @AppSingleton
    fun bindsJoinPoster(posterImpl: JoiningPosterImpl): JoiningPoster

}
