package org.stepic.droid.di.tags

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepic.droid.ui.fragments.TagFragment
import org.stepik.android.model.Tag
import org.stepik.android.view.injection.tags.TagsDataModule

@TagScope
@Subcomponent(modules = [TagsDataModule::class])
interface TagComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): TagComponent

        @BindsInstance
        fun tag(tag: Tag): Builder
    }

    fun inject(tagFragment: TagFragment)
}
