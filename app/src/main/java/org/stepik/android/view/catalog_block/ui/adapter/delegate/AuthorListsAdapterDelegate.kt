package org.stepik.android.view.catalog_block.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.header_catalog_block.*
import kotlinx.android.synthetic.main.item_author_lists.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.domain.catalog_block.model.CatalogAuthor
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.catalog_block.mapper.AuthorCountMapper
import org.stepik.android.view.catalog_block.model.CatalogItem
import org.stepik.android.view.catalog_block.ui.delegate.CatalogBlockHeaderDelegate
import ru.nobird.android.core.model.cast
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class AuthorListsAdapterDelegate(
    private val authorCountMapper: AuthorCountMapper,
    private val onAuthorClick: (Long) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Block && data.catalogBlockStateWrapper is CatalogBlockStateWrapper.AuthorList

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        AuthorListViewHolder(createView(parent, R.layout.item_author_lists))

    private inner class AuthorListViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CatalogItem>(containerView), LayoutContainer {

        private val catalogBlockTitleDelegate =
            CatalogBlockHeaderDelegate(catalogBlockContainer, null)

        private val adapter = DefaultDelegateAdapter<CatalogAuthor>()
            .also {
                it += AuthorListAdapterDelegate(onAuthorClick)
            }

        init {
            val rowCount = context.resources.getInteger(R.integer.author_lists_default_rows)
            authorListsRecycler.layoutManager =
                TableLayoutManager(
                    context,
                    horizontalSpanCount = context.resources.getInteger(R.integer.author_lists_default_columns),
                    verticalSpanCount = rowCount,
                    orientation = RecyclerView.HORIZONTAL,
                    reverseLayout = false
                )
            authorListsRecycler.setRecycledViewPool(sharedViewPool)
            authorListsRecycler.setHasFixedSize(true)
            authorListsRecycler.adapter = adapter

            val snapHelper = CoursesSnapHelper(rowCount)
            snapHelper.attachToRecyclerView(authorListsRecycler)
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