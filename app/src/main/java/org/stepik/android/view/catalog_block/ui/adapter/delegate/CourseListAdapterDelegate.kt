package org.stepik.android.view.catalog_block.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_course_list.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.presentation.course_list_redux.CourseListFeature
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.catalog_block.model.CatalogItem
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListItemAdapterDelegate
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListPlaceHolderAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseListAdapterDelegate(
    private val analytic: Analytic,
    private val isHandleInAppPurchase: Boolean
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Block

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        CourseCollectionViewHolder(createView(parent, R.layout.item_course_list))

    private inner class CourseCollectionViewHolder(root: View) : DelegateViewHolder<CatalogItem>(root) {

        private var courseCollection: CatalogBlockItem? = null

        private val courseListTitle = root.courseListTitle
        private val courseListDescription = root.courseListDescription
        private val courseListCoursesRecycler = root.courseListCoursesRecycler
        private val courseListPlaceholderEmpty = root.courseListPlaceholderEmpty
        private val courseListTitleContainer = root.courseListTitleContainer
        private val courseListPlaceholderNoConnection = root.courseListPlaceholderNoConnection

        private val skeletonCount = root.resources.getInteger(R.integer.course_list_rows) * root.resources.getInteger(R.integer.course_list_columns)
        private val courseItemsSkeleton: List<CourseListItem> = List(skeletonCount) { CourseListItem.PlaceHolder() }
        private val courseItemAdapter: DefaultDelegateAdapter<CourseListItem> = DefaultDelegateAdapter()
        private val viewStateDelegate = ViewStateDelegate<CourseListFeature.State>()

        init {
            viewStateDelegate.addState<CourseListFeature.State.Idle>(courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListFeature.State.Loading>(courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListFeature.State.Content>(courseListTitleContainer, courseListDescription, courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListFeature.State.Empty>(courseListPlaceholderEmpty)
            viewStateDelegate.addState<CourseListFeature.State.NetworkError>(courseListPlaceholderNoConnection)

            val onClickListener = View.OnClickListener {}

            courseListDescription.setOnClickListener(onClickListener)
            courseListTitleContainer.setOnClickListener(onClickListener)

            courseListPlaceholderEmpty.setOnClickListener {  }
            courseListPlaceholderEmpty.setPlaceholderText(R.string.empty_courses_popular)
            courseListPlaceholderNoConnection.setOnClickListener {}
            courseListPlaceholderNoConnection.setText(R.string.internet_problem)

            courseItemAdapter += CourseListPlaceHolderAdapterDelegate()
            courseItemAdapter += CourseListItemAdapterDelegate(
                analytic = analytic,
                onItemClicked = {},
                onContinueCourseClicked = {},
                isHandleInAppPurchase = isHandleInAppPurchase
            )

            with(courseListCoursesRecycler) {
                adapter = courseItemAdapter
                val rowCount = resources.getInteger(R.integer.course_list_rows)
                val columnsCount = resources.getInteger(R.integer.course_list_columns)
                layoutManager = TableLayoutManager(context, columnsCount, rowCount, RecyclerView.HORIZONTAL, false)
                itemAnimator?.changeDuration = 0
                val snapHelper = CoursesSnapHelper(rowCount)
                snapHelper.attachToRecyclerView(this)
                setRecycledViewPool(sharedViewPool)
                setHasFixedSize(true)
            }
        }

        override fun onBind(data: CatalogItem) {
            data as CatalogItem.Block
            val catalogBlockCourseListItem = data.catalogBlockStateWrapper as CatalogBlockStateWrapper.CourseList
            courseCollection = catalogBlockCourseListItem.catalogBlockItem
            courseListTitle.text = catalogBlockCourseListItem.catalogBlockItem.title
            courseListDescription.setPlaceholderText(catalogBlockCourseListItem.catalogBlockItem.description)
            setState(catalogBlockCourseListItem.state)
        }

        fun setState(state: CourseListFeature.State) {
            viewStateDelegate.switchState(state)
            when (state) {
                is CourseListFeature.State.Idle -> {
                    courseItemAdapter.items = emptyList()
                    courseItemAdapter.notifyDataSetChanged()
                }

                is CourseListFeature.State.Loading -> {
                    courseItemAdapter.items = courseItemsSkeleton
                }

                is CourseListFeature.State.Content -> {
                    courseItemAdapter.items = state.courseListItems
                }

                else ->
                    courseItemAdapter.items = emptyList()
            }
        }
    }
}