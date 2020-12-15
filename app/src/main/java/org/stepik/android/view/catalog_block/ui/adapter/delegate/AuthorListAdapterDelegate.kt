package org.stepik.android.view.catalog_block.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_course_list_new.view.*
import kotlinx.android.synthetic.main.view_container_block.view.*
import org.stepic.droid.R
import org.stepik.android.domain.catalog_block.model.AuthorCatalogBlockContentItem
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem
import org.stepik.android.presentation.author_list.AuthorListFeature
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.view.catalog_block.model.CatalogItem
import org.stepik.android.view.catalog_block.ui.delegate.CatalogBlockTitleDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate

class AuthorListAdapterDelegate(
    private val onAuthorClick: (Long) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Block && data.catalogBlockStateWrapper is CatalogBlockStateWrapper.AuthorList

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        AuthorListViewHolder(createView(parent, R.layout.item_course_list_new))

    private inner class AuthorListViewHolder(root: View) : DelegateViewHolder<CatalogItem>(root) {
        private var courseCollection: CatalogBlockItem? = null

        private val courseListCoursesRecycler = root.courseListCoursesRecycler
        private val courseListTitleContainer = root.catalogBlockContainer

        private val catalogBlockTitleDelegate = CatalogBlockTitleDelegate(courseListTitleContainer)
        private val authorItemAdapter: DefaultDelegateAdapter<AuthorCatalogBlockContentItem> = DefaultDelegateAdapter()
        private val viewStateDelegate = ViewStateDelegate<AuthorListFeature.State>()

        init {
            viewStateDelegate.addState<AuthorListFeature.State.Idle>(courseListCoursesRecycler)
            viewStateDelegate.addState<AuthorListFeature.State.Content>(courseListTitleContainer, courseListCoursesRecycler)

            // TODO Add adapter delegate for author items
//            authorItemAdapter +=
        }

        override fun onBind(data: CatalogItem) {
            data as CatalogItem.Block
            val catalogBlockAuthorListItem = data.catalogBlockStateWrapper as CatalogBlockStateWrapper.AuthorList
            catalogBlockTitleDelegate.setInformation(catalogBlockAuthorListItem.catalogBlockItem)
//            catalogBlockTitleDelegate.setCount(catalogBlockAuthorListItem.catalogBlockItem)
            setState(catalogBlockAuthorListItem.state)
        }

        fun setState(state: AuthorListFeature.State) {
            viewStateDelegate.switchState(state)
            when (state) {
                is AuthorListFeature.State.Idle -> {
                    authorItemAdapter.items = emptyList()
                }

                is AuthorListFeature.State.Content ->
                    authorItemAdapter.items = state.authorListItems
            }
        }
    }
}