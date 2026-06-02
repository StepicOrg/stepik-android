package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemAuthorListBinding
import org.stepik.android.domain.catalog.model.CatalogAuthor
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.catalog.mapper.AuthorCountMapper
import org.stepik.android.view.catalog.model.CatalogItem
import org.stepik.android.view.catalog.ui.delegate.CatalogBlockHeaderDelegate
import ru.nobird.app.core.model.cast
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class AuthorListAdapterDelegate(
    private val authorCountMapper: AuthorCountMapper,
    private val onAuthorClick: (Long) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Block && data.catalogBlockStateWrapper is CatalogBlockStateWrapper.AuthorList

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        AuthorListViewHolder(createView(parent, R.layout.item_author_list))

    private inner class AuthorListViewHolder(root: android.view.View) : DelegateViewHolder<CatalogItem>(root) {
        private val viewBinding: ItemAuthorListBinding by viewBinding { ItemAuthorListBinding.bind(root) }

        private val catalogBlockTitleDelegate =
            CatalogBlockHeaderDelegate(viewBinding.catalogBlockHeader.root, null)

        private val adapter = DefaultDelegateAdapter<CatalogAuthor>()
            .also {
                it += AuthorAdapterDelegate(onAuthorClick)
            }

        init {
            val rowCount = context.resources.getInteger(R.integer.author_lists_default_rows)
            viewBinding.authorListRecycler.layoutManager =
                TableLayoutManager(
                    context,
                    horizontalSpanCount = context.resources.getInteger(R.integer.author_lists_default_columns),
                    verticalSpanCount = rowCount,
                    orientation = RecyclerView.HORIZONTAL,
                    reverseLayout = false
                )
            viewBinding.authorListRecycler.setRecycledViewPool(sharedViewPool)
            viewBinding.authorListRecycler.setHasFixedSize(true)
            viewBinding.authorListRecycler.adapter = adapter

            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(viewBinding.authorListRecycler)
        }

        override fun onBind(data: CatalogItem) {
            val authorLists = data
                .cast<CatalogItem.Block>()
                .catalogBlockStateWrapper
                .cast<CatalogBlockStateWrapper.AuthorList>()

            adapter.items = authorLists.content.authors
            catalogBlockTitleDelegate.setInformation(authorLists.catalogBlockItem)

            val count = authorLists.content.authors.size
            catalogBlockTitleDelegate.setCount(authorCountMapper.mapAuthorCountToString(context, count))
        }
    }
}
