package org.stepic.droid.di.tags

import dagger.BindsInstance
import dagger.Component
import dagger.Subcomponent
import org.stepic.droid.model.Tag
import org.stepic.droid.ui.fragments.TagFragment

@TagScope
@Subcomponent(modules = arrayOf(TagModule::class))
interface TagComponent {

    @Component.Builder
    interface Builder {
        fun build(): TagComponent

        @BindsInstance
        fun tag(tag: Tag): Builder
    }

    fun inject(tagFragment: TagFragment)
}
