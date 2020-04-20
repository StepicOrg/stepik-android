package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.android.synthetic.main.view_course_languages.view.*
import org.stepic.droid.R
import org.stepic.droid.model.StepikFilter
import org.stepik.android.presentation.base.PresenterViewHolder
import org.stepik.android.presentation.catalog.model.CatalogItem
import org.stepik.android.presentation.filter.FiltersPresenter
import org.stepik.android.presentation.filter.FiltersView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import java.util.EnumSet

class FiltersAdapterDelegate : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is FiltersPresenter

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        FiltersViewHolder(createView(parent, R.layout.view_course_languages)) as DelegateViewHolder<CatalogItem>

    private class FiltersViewHolder(root: View) : PresenterViewHolder<FiltersView, FiltersPresenter>(root), FiltersView {
        private val viewStateDelegate = ViewStateDelegate<FiltersView.State>()
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
                        itemData?.onFilterChanged(filters)
                    }
                }
            languages.addOnButtonCheckedListener(toggleListener)

            viewStateDelegate.addState<FiltersView.State.Idle>()
            viewStateDelegate.addState<FiltersView.State.Empty>()
            viewStateDelegate.addState<FiltersView.State.FiltersLoaded>(itemView)
        }

        override fun setState(state: FiltersView.State) {
            viewStateDelegate.switchState(state)
            if (state is FiltersView.State.FiltersLoaded) {
                if (StepikFilter.RUSSIAN in state.filters) {
                    languages.check(R.id.languageRu)
                }

                if (StepikFilter.ENGLISH in state.filters) {
                    languages.check(R.id.languageEn)
                }
            }
        }

        override fun attachView(data: FiltersPresenter) {
            data.attachView(this)
        }

        override fun detachView(data: FiltersPresenter) {
            data.detachView(this)
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