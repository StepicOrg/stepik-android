package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.android.synthetic.main.view_course_languages.view.*
import org.stepic.droid.R
import org.stepic.droid.model.StepikFilter
import org.stepik.android.presentation.filter.FiltersFeature
import org.stepik.android.view.catalog_block.model.CatalogItem
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import java.util.EnumSet

class FiltersAdapterDelegate(
    val onFiltersChanged: (filters: EnumSet<StepikFilter>) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Filters

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        FiltersViewHolder(createView(parent, R.layout.view_course_languages))

    private inner class FiltersViewHolder(root: View) : DelegateViewHolder<CatalogItem>(root) {
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
                        onFiltersChanged(filters)
                    }
                }
            languages.addOnButtonCheckedListener(toggleListener)

            viewStateDelegate.addState<FiltersFeature.State.Idle>()
            viewStateDelegate.addState<FiltersFeature.State.Loading>()
            viewStateDelegate.addState<FiltersFeature.State.Empty>()
            viewStateDelegate.addState<FiltersFeature.State.FiltersLoaded>(itemView)
        }

        override fun onBind(data: CatalogItem) {
            data as CatalogItem.Filters
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