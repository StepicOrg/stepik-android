package org.stepic.droid.di.tags

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepic.droid.model.Tag
import org.stepic.droid.ui.fragments.TagFragment

@TagScope
@Subcomponent()
interface TagComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): TagComponent

        @BindsInstance
        fun tag(tag: Tag): Builder
    }

    fun inject(tagFragment: TagFragment)
}
