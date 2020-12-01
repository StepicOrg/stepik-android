package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.android.synthetic.main.view_course_languages.view.*
import org.stepic.droid.R
import org.stepic.droid.model.StepikFilter
import org.stepik.android.presentation.filter.FiltersFeature
import org.stepik.android.view.catalog_block.model.CatalogBlockItem
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import timber.log.Timber
import java.util.EnumSet

class FiltersAdapterDelegate(
    val onFiltersChanged: (filters: EnumSet<StepikFilter>) -> Unit
) : AdapterDelegate<CatalogBlockItem, DelegateViewHolder<CatalogBlockItem>>() {
    override fun isForViewType(position: Int, data: CatalogBlockItem): Boolean =
        data is CatalogBlockItem.FiltersBlock

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogBlockItem> =
        FiltersViewHolder(createView(parent, R.layout.view_course_languages))

    private inner class FiltersViewHolder(root: View) : DelegateViewHolder<CatalogBlockItem>(root) {
        private val viewStateDelegate = ViewStateDelegate<FiltersFeature.State>()
        private val languages = itemView.languages

        init {
            val toggleListener =
                object : MaterialButtonToggleGroup.OnButtonCheckedListener {
                    override fun onButtonChecked(group: MaterialButtonToggleGroup, checkedId: Int, isChecked: Boolean) {
                        if (languages.checkedButtonIds.isEmpty()) {
                            group.removeOnButtonCheckedListener(this)
                            group.check(checkedId)
                            group.addOnButtonCheckedListener(this)
                            return
                        }
                        val filters = composeFilters()
                        Timber.d("Go")

                        onFiltersChanged(filters)
                    }
                }
            languages.addOnButtonCheckedListener(toggleListener)

            viewStateDelegate.addState<FiltersFeature.State.Idle>()
            viewStateDelegate.addState<FiltersFeature.State.Loading>()
            viewStateDelegate.addState<FiltersFeature.State.Empty>()
            viewStateDelegate.addState<FiltersFeature.State.FiltersLoaded>(itemView)
        }

        override fun onBind(data: CatalogBlockItem) {
            data as CatalogBlockItem.FiltersBlock
            render(data.state)
        }

        private fun render(state: FiltersFeature.State) {
            viewStateDelegate.switchState(state)
            if (state is FiltersFeature.State.FiltersLoaded) {
                if (StepikFilter.RUSSIAN in state.filters) {
                    languages.check(R.id.languageRu)
                }

                if (StepikFilter.ENGLISH in state.filters) {
                    languages.check(R.id.languageEn)
                }
            }
        }

        private fun composeFilters(): EnumSet<StepikFilter> {
            val filters = EnumSet.noneOf(StepikFilter::class.java)
            if (R.id.languageRu in languages.checkedButtonIds) {
                filters.add(StepikFilter.RUSSIAN)
            }

            if (R.id.languageEn in languages.checkedButtonIds) {
                filters.add(StepikFilter.ENGLISH)
            }
            return filters
        }
    }
}