package org.stepic.droid.di.tags

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepic.droid.ui.fragments.TagFragment
import org.stepik.android.model.structure.Tag

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
