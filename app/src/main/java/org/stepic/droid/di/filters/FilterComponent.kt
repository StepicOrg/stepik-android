package org.stepic.droid.di.filters

import dagger.Subcomponent

@FilterScope
@Subcomponent
interface FilterComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): FilterComponent
    }

}
