package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.JustifyContent
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemBlockSimpleCourseListsGridBinding
import org.stepik.android.domain.catalog.model.CatalogCourseList
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.view.catalog.mapper.CourseCountMapper
import org.stepik.android.view.catalog.model.CatalogItem
import org.stepik.android.view.catalog.ui.delegate.CatalogBlockHeaderDelegate
import ru.nobird.app.core.model.cast
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class SimpleCourseListsGridAdapterDelegate(
    private val courseCountMapper: CourseCountMapper,
    private val onCourseListClicked: (CatalogCourseList) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Block && data.catalogBlockStateWrapper is CatalogBlockStateWrapper.SimpleCourseListsGrid

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        ViewHolder(createView(parent, R.layout.item_block_simple_course_lists_grid))

    private inner class ViewHolder(root: android.view.View) : DelegateViewHolder<CatalogItem>(root) {
        private val viewBinding: ItemBlockSimpleCourseListsGridBinding by viewBinding { ItemBlockSimpleCourseListsGridBinding.bind(root) }

        private val catalogBlockTitleDelegate =
            CatalogBlockHeaderDelegate(viewBinding.catalogBlockHeader.root, null)

        private val adapter = DefaultDelegateAdapter<CatalogCourseList>()
            .also {
                it += SimpleCourseListGridFirstAdapter(courseCountMapper, onCourseListClicked)
                it += SimpleCourseListGridAdapterDelegate(onCourseListClicked)
            }

        init {
            viewBinding.courseListsRecycler.layoutManager =
                FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP)
                    .apply { justifyContent = JustifyContent.FLEX_START }

            viewBinding.courseListsRecycler.setRecycledViewPool(sharedViewPool)
            viewBinding.courseListsRecycler.setHasFixedSize(true)
            viewBinding.courseListsRecycler.adapter = adapter
        }

        override fun onBind(data: CatalogItem) {
            super.onBind(data)
            val simpleCourseListGrid = data
                .cast<CatalogItem.Block>()
                .catalogBlockStateWrapper
                .cast<CatalogBlockStateWrapper.SimpleCourseListsGrid>()

            adapter.items = simpleCourseListGrid.content.courseLists
            catalogBlockTitleDelegate.setInformation(simpleCourseListGrid.catalogBlockItem)

            val count = simpleCourseListGrid.content.courseLists.size
            catalogBlockTitleDelegate.setCount(context.resources.getQuantityString(R.plurals.catalog_course_lists, count, count))
        }
    }
}
