package org.stepik.android.view.catalog_block.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_author_list.view.*
import kotlinx.android.synthetic.main.view_container_block.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.domain.catalog_block.model.AuthorCatalogBlockContentItem
import org.stepik.android.domain.catalog_block.model.CatalogBlockContent
import org.stepik.android.presentation.author_list.AuthorListFeature
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.view.author_list.ui.adapter.delegate.AuthorListItemAdapterDelegate
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.catalog_block.model.CatalogItem
import org.stepik.android.view.catalog_block.ui.delegate.CatalogBlockTitleDelegate
import ru.nobird.android.core.model.safeCast
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate

class AuthorListAdapterDelegate(
    private val onBlockSeen: (String, CatalogBlockContent.AuthorCourseList) -> Unit,
    private val onAuthorClick: (Long) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    companion object {
        private const val MAX_AUTHOR_COUNT = 99
    }

    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Block && data.catalogBlockStateWrapper is CatalogBlockStateWrapper.AuthorList

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        AuthorListViewHolder(createView(parent, R.layout.item_author_list))

    private inner class AuthorListViewHolder(root: View) : DelegateViewHolder<CatalogItem>(root) {

        private val authorListRecycler = root.authorListRecycler
        private val catalogBlockContainer = root.catalogBlockContainer

        private val catalogBlockTitleDelegate = CatalogBlockTitleDelegate(catalogBlockContainer)
        private val authorItemAdapter: DefaultDelegateAdapter<AuthorCatalogBlockContentItem> = DefaultDelegateAdapter()
        private val viewStateDelegate = ViewStateDelegate<AuthorListFeature.State>()

        init {
            viewStateDelegate.addState<AuthorListFeature.State.Idle>(authorListRecycler)
            viewStateDelegate.addState<AuthorListFeature.State.Content>(catalogBlockContainer, authorListRecycler)

            authorItemAdapter += AuthorListItemAdapterDelegate { onAuthorClick(it) }

            with(authorListRecycler) {
                adapter = authorItemAdapter
                val rowCount = resources.getInteger(R.integer.course_list_rows) // TODO Maybe make custom resources?
                val columnsCount = resources.getInteger(R.integer.course_list_columns)
                layoutManager = TableLayoutManager(context, columnsCount, rowCount, RecyclerView.HORIZONTAL, false)
                itemAnimator?.changeDuration = 0
                val snapHelper = CoursesSnapHelper(rowCount)
                snapHelper.attachToRecyclerView(this)
                setHasFixedSize(true)
            }
        }

        override fun onBind(data: CatalogItem) {
            data as CatalogItem.Block
            val catalogBlockAuthorListItem = data.catalogBlockStateWrapper as CatalogBlockStateWrapper.AuthorList
            catalogBlockTitleDelegate.setInformation(catalogBlockAuthorListItem.catalogBlockItem)
            catalogBlockAuthorListItem
                .catalogBlockItem
                .content
                .safeCast<CatalogBlockContent.AuthorCourseList>()
                ?.let {
                    val countString = getCountString(it.content.size)
                    catalogBlockTitleDelegate.setCount(countString)
                    onBlockSeen(catalogBlockAuthorListItem.id, it)
                }

            render(catalogBlockAuthorListItem.state)
        }

        private fun render(state: AuthorListFeature.State) {
            viewStateDelegate.switchState(state)
            when (state) {
                is AuthorListFeature.State.Idle -> {
                    authorItemAdapter.items = emptyList()
                }

                is AuthorListFeature.State.Content ->
                    authorItemAdapter.items = state.authorListItems
            }
        }

        private fun getCountString(itemCount: Int): String =
            if (itemCount > MAX_AUTHOR_COUNT) {
                context.resources.getString(R.string.author_max_count)
            } else {
                context.resources.getQuantityString(R.plurals.author_count, itemCount, itemCount)
            }
    }
}