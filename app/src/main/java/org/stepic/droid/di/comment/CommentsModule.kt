package org.stepic.droid.di.comment

import dagger.Binds
import dagger.Module
import org.stepic.droid.base.Client
import org.stepic.droid.base.ClientImpl
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.base.ListenerContainerImpl
import org.stepic.droid.core.comments.CommentsPosterImpl
import org.stepic.droid.core.comments.contract.CommentsListener
import org.stepic.droid.core.comments.contract.CommentsPoster

@Module
interface CommentsModule {

    @Binds
    @CommentsScope
    fun bindsClient(clientImpl: ClientImpl<CommentsListener>): Client<CommentsListener>

    @Binds
    @CommentsScope
    fun bindContainer(listenerContainer: ListenerContainerImpl<CommentsListener>): ListenerContainer<CommentsListener>

    @Binds
    @CommentsScope
    fun bindsPoster(posterImpl: CommentsPosterImpl): CommentsPoster

}