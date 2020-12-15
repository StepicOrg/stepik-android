package org.stepik.android.view.catalog_block.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_block_simple_course_lists_default.*
import kotlinx.android.synthetic.main.view_container_block.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.domain.catalog_block.model.StandardCatalogBlockContentItem
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.catalog_block.model.CatalogItem
import org.stepik.android.view.catalog_block.ui.delegate.CatalogBlockTitleDelegate
import ru.nobird.android.core.model.cast
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class SimpleCourseListsDefaultAdapterDelegate(
    private val onCourseListClicked: (StandardCatalogBlockContentItem) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Block && data.catalogBlockStateWrapper is CatalogBlockStateWrapper.SimpleCourseListsDefault

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        ViewHolder(createView(parent, R.layout.item_block_simple_course_lists_default))

    private inner class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CatalogItem>(containerView), LayoutContainer {
        private val catalogBlockTitleDelegate =
            CatalogBlockTitleDelegate(catalogBlockContainer, null)

        private val adapter = DefaultDelegateAdapter<StandardCatalogBlockContentItem>()
            .also {
                it += SimpleCourseListDefaultAdapterDelegate(onCourseListClicked)
            }

        init {
            courseListsRecycler.layoutManager =
                TableLayoutManager(
                    context,
                    horizontalSpanCount = 2,
                    verticalSpanCount = 2,
                    orientation = LinearLayoutManager.HORIZONTAL,
                    reverseLayout = false
                )
            courseListsRecycler.setRecycledViewPool(sharedViewPool)
            courseListsRecycler.setHasFixedSize(true)
            courseListsRecycler.adapter = adapter

            val snapHelper = CoursesSnapHelper(2)
            snapHelper.attachToRecyclerView(courseListsRecycler)

        }

        override fun onBind(data: CatalogItem) {
            super.onBind(data)
            val simpleCourseListsDefault = data
                .cast<CatalogItem.Block>()
                .catalogBlockStateWrapper
                .cast<CatalogBlockStateWrapper.SimpleCourseListsDefault>()

            adapter.items = simpleCourseListsDefault.content.content
            catalogBlockTitleDelegate.setInformation(simpleCourseListsDefault.catalogBlockItem)
            catalogBlockTitleDelegate.setCount(simpleCourseListsDefault.content.content.size.toString())
        }
    }
}