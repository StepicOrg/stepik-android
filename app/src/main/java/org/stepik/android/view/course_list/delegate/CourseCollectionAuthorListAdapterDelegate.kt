package org.stepik.android.view.course_list.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_collection_horizontal_list.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.domain.catalog.model.CatalogAuthor
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.catalog.ui.adapter.delegate.AuthorAdapterDelegate
import ru.nobird.android.core.model.cast
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseCollectionAuthorListAdapterDelegate(
    private val onAuthorClick: (Long) -> Unit
) : AdapterDelegate<CourseListItem, DelegateViewHolder<CourseListItem>>() {
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CourseListItem): Boolean =
        data is CourseListItem.SimilarAuthors

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseListItem> =
        AuthorListViewHolder(createView(parent, R.layout.item_collection_horizontal_list))

    private inner class AuthorListViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CourseListItem>(containerView), LayoutContainer {

        private val adapter = DefaultDelegateAdapter<CatalogAuthor>()
            .also {
                it += AuthorAdapterDelegate(onAuthorClick)
            }

        init {
            val rowCount = context.resources.getInteger(R.integer.author_lists_default_rows)
            horizontalListRecycler.layoutManager =
                TableLayoutManager(
                    context,
                    horizontalSpanCount = context.resources.getInteger(R.integer.author_lists_default_columns),
                    verticalSpanCount = rowCount,
                    orientation = RecyclerView.HORIZONTAL,
                    reverseLayout = false
                )
            horizontalListRecycler.setRecycledViewPool(sharedViewPool)
            horizontalListRecycler.setHasFixedSize(true)
            horizontalListRecycler.adapter = adapter

            val snapHelper = CoursesSnapHelper(rowCount)
            snapHelper.attachToRecyclerView(horizontalListRecycler)
        }

        override fun onBind(data: CourseListItem) {
            val authorLists = data
                .cast<CourseListItem.SimilarAuthors>()
                .similarAuthors

            containerTitle.text = itemView.resources.getString(R.string.authors_title)

            adapter.items = authorLists
        }
    }
}