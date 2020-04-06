package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import kotlinx.android.synthetic.main.view_course_languages.view.*
import org.stepic.droid.R
import org.stepic.droid.model.StepikFilter
import org.stepik.android.presentation.base.PresenterViewHolder
import org.stepik.android.presentation.catalog.CatalogItem
import org.stepik.android.presentation.catalog.FiltersPresenter
import org.stepik.android.presentation.catalog.FiltersView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import java.util.EnumSet

class FiltersAdapterDelegate(
    private val onFiltersChanged: (EnumSet<StepikFilter>) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is FiltersPresenter

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        FiltersViewHolder(createView(parent, R.layout.view_course_languages), onFiltersChanged =  onFiltersChanged) as DelegateViewHolder<CatalogItem>

    private class FiltersViewHolder(
        root: View,
        onFiltersChanged: (EnumSet<StepikFilter>) -> Unit
    ) : PresenterViewHolder<FiltersView, FiltersPresenter>(root), FiltersView {

        private val languageRu = itemView.languageRu
        private val languageEn = itemView.languageEn

        private val viewStateDelegate = ViewStateDelegate<FiltersView.State>()

        init {
            val onClickListener = View.OnClickListener { checkableView ->
                checkableView as Checkable
                if (checkableView.isChecked) {
                    // skip click event
                    return@OnClickListener
                }
                languageRu.toggle()
                languageEn.toggle()
                val filters = composeFilters()
                onFiltersChanged(filters)
            }

            languageRu.setOnClickListener(onClickListener)
            languageEn.setOnClickListener(onClickListener)

            viewStateDelegate.addState<FiltersView.State.Idle>()
            viewStateDelegate.addState<FiltersView.State.Empty>()
            viewStateDelegate.addState<FiltersView.State.FiltersLoaded>(itemView)
        }

        override fun setState(state: FiltersView.State) {
            viewStateDelegate.switchState(state)
            if (state is FiltersView.State.FiltersLoaded) {
                updateCheckableView(languageRu, state.filters.contains(StepikFilter.RUSSIAN))
                updateCheckableView(languageEn, state.filters.contains(StepikFilter.ENGLISH))
            }
        }

        override fun attachView(data: FiltersPresenter) {
            data.attachView(this)
        }

        override fun detachView(data: FiltersPresenter) {
            data.detachView(this)
        }
        private fun updateCheckableView(view: Checkable, shouldBeChecked: Boolean) {
            if (view.isChecked != shouldBeChecked) {
                view.isChecked = shouldBeChecked
            }
        }

        private fun composeFilters(): EnumSet<StepikFilter> {
            val filters = EnumSet.noneOf(StepikFilter::class.java)
            if (languageRu.isChecked) {
                filters.add(StepikFilter.RUSSIAN)
            }

            if (languageEn.isChecked) {
                filters.add(StepikFilter.ENGLISH)
            }
            return filters
        }
    }
}