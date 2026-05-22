package org.stepik.android.view.injection.filter_search

import dagger.Subcomponent
import org.stepik.android.view.filter.ui.dialog.FilterSearchBottomSheetDialogFragment

@Subcomponent(modules = [FilterSearchModule::class])
interface FilterSearchComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): FilterSearchComponent
    }

    fun inject(filterSearchBottomSheetDialogFragment: FilterSearchBottomSheetDialogFragment)
}