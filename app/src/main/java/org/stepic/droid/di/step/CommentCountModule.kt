package org.stepic.droid.di.step

import dagger.Binds
import dagger.Module
import org.stepic.droid.base.Client
import org.stepic.droid.base.ClientImpl
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.base.ListenerContainerImpl
import org.stepic.droid.core.comment_count.CommentCountPosterImpl
import org.stepic.droid.core.comment_count.contract.CommentCountListener
import org.stepic.droid.core.comment_count.contract.CommentCountPoster

@Module
interface CommentCountModule {

    @Binds
    @StepScope
    fun bindsClient(clientImpl: ClientImpl<CommentCountListener>): Client<CommentCountListener>

    @Binds
    @StepScope
    fun bindContainer(listenerContainer: ListenerContainerImpl<CommentCountListener>): ListenerContainer<CommentCountListener>

    @Binds
    @StepScope
    fun bindsPoster(posterImpl: CommentCountPosterImpl): CommentCountPoster

}
