package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_catalog_tags.view.*
import org.stepic.droid.R
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.ui.adapters.TagsAdapter
import org.stepik.android.model.Tag
import org.stepik.android.presentation.base.PresenterViewHolder
import org.stepik.android.presentation.catalog.FiltersView
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import java.util.EnumSet

class TagsAdapterDelegate(
    private val onTagClicked: (Tag) -> Unit
) : AdapterDelegate<PresenterBase<FiltersView>, DelegateViewHolder<PresenterBase<FiltersView>>>() {
    override fun isForViewType(position: Int, data: PresenterBase<FiltersView>): Boolean =
        data is FiltersView

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<PresenterBase<FiltersView>> =
        TagsViewHolder(createView(parent, R.layout.view_catalog_tags), onTagClicked = onTagClicked)

    private class TagsViewHolder(root: View, onTagClicked: (Tag) -> Unit) : PresenterViewHolder<FiltersView>(root), FiltersView {

        private val tagsRecyclerView = root.tagsRecycler
        private val tagsAdapter = TagsAdapter(onTagClicked)

        init {
            tagsRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            tagsRecyclerView.adapter = tagsAdapter
        }

        // TODO Binding

        override fun onFiltersPrepared(filters: EnumSet<StepikFilter>) {
            TODO("Not yet implemented")
        }

        override fun attachView(data: PresenterBase<FiltersView>) {
            data.attachView(this)
        }

        override fun detachView(data: PresenterBase<FiltersView>) {
            data.detachView(this)
        }
    }
}