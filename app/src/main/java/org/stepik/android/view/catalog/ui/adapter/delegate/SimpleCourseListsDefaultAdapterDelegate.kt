package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.header_catalog_block.*
import kotlinx.android.synthetic.main.item_block_simple_course_lists_default.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.domain.catalog.model.CatalogCourseList
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.catalog.mapper.CourseCountMapper
import org.stepik.android.view.catalog.model.CatalogItem
import org.stepik.android.view.catalog.ui.delegate.CatalogBlockHeaderDelegate
import ru.nobird.app.core.model.cast
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class SimpleCourseListsDefaultAdapterDelegate(
    private val courseCountMapper: CourseCountMapper,
    private val onCourseListClicked: (CatalogCourseList) -> Unit
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
            CatalogBlockHeaderDelegate(catalogBlockContainer, null)

        private val adapter = DefaultDelegateAdapter<CatalogCourseList>()
            .also {
                it += SimpleCourseListDefaultAdapterDelegate(courseCountMapper, onCourseListClicked)
            }

        init {
            val rowCount = context.resources.getInteger(R.integer.simple_course_lists_default_rows)
            courseListsRecycler.layoutManager =
                TableLayoutManager(
                    context,
                    horizontalSpanCount = context.resources.getInteger(R.integer.simple_course_lists_default_columns),
                    verticalSpanCount = rowCount,
                    orientation = LinearLayoutManager.HORIZONTAL,
                    reverseLayout = false
                )
            courseListsRecycler.setRecycledViewPool(sharedViewPool)
            courseListsRecycler.setHasFixedSize(true)
            courseListsRecycler.adapter = adapter

            val snapHelper = CoursesSnapHelper(rowCount)
            snapHelper.attachToRecyclerView(courseListsRecycler)
        }

        override fun onBind(data: CatalogItem) {
            val simpleCourseListsDefault = data
                .cast<CatalogItem.Block>()
                .catalogBlockStateWrapper
                .cast<CatalogBlockStateWrapper.SimpleCourseListsDefault>()

            adapter.items = simpleCourseListsDefault.content.courseLists
            catalogBlockTitleDelegate.setInformation(simpleCourseListsDefault.catalogBlockItem)

            val count = simpleCourseListsDefault.content.courseLists.size
            catalogBlockTitleDelegate.setCount(context.resources.getQuantityString(R.plurals.catalog_course_lists, count, count))
        }
    }
}