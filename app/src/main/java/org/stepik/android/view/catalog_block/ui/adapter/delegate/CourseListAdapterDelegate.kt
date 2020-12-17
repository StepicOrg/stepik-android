package org.stepik.android.view.catalog_block.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_course_list_new.view.*
import kotlinx.android.synthetic.main.view_container_block.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.domain.catalog_block.model.CatalogBlockContent
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list_redux.CourseListFeature
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.catalog_block.model.CatalogItem
import org.stepik.android.view.catalog_block.ui.delegate.CatalogBlockTitleDelegate
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListItemAdapterDelegate
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListPlaceHolderAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.core.model.safeCast
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseListAdapterDelegate(
    private val analytic: Analytic,
    private val isHandleInAppPurchase: Boolean,
    private val onTitleClick: (Long) -> Unit,
    private val onBlockSeen: (String, CatalogBlockContent.FullCourseList) -> Unit,
    private val onCourseContinueClicked: (Course, CourseViewSource, CourseContinueInteractionSource) -> Unit,
    private val onCourseClicked: (CourseListItem.Data) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    companion object {
        private const val MAX_COURSE_COUNT = 99
    }
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Block && data.catalogBlockStateWrapper is CatalogBlockStateWrapper.CourseList

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        CourseCollectionViewHolder(createView(parent, R.layout.item_course_list_new))

    private inner class CourseCollectionViewHolder(root: View) : DelegateViewHolder<CatalogItem>(root) {

        private var courseCollection: CatalogBlockItem? = null

        private val courseListCoursesRecycler = root.courseListCoursesRecycler
        private val courseListTitleContainer = root.catalogBlockContainer

        private val catalogBlockTitleDelegate = CatalogBlockTitleDelegate(courseListTitleContainer) {
            val collection = (courseCollection?.content as? CatalogBlockContent.FullCourseList) ?: return@CatalogBlockTitleDelegate
            onTitleClick(collection.content.id)
        }

        private val skeletonCount = root.resources.getInteger(R.integer.course_list_rows) * root.resources.getInteger(R.integer.course_list_columns)
        private val courseItemsSkeleton: List<CourseListItem> = List(skeletonCount) { CourseListItem.PlaceHolder() }
        private val courseItemAdapter: DefaultDelegateAdapter<CourseListItem> = DefaultDelegateAdapter()
        private val viewStateDelegate = ViewStateDelegate<CourseListFeature.State>()

        init {
            viewStateDelegate.addState<CourseListFeature.State.Idle>(courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListFeature.State.Loading>(courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListFeature.State.Content>(courseListTitleContainer, courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListFeature.State.Empty>()
            viewStateDelegate.addState<CourseListFeature.State.NetworkError>()

            courseItemAdapter += CourseListPlaceHolderAdapterDelegate()
            courseItemAdapter += CourseListItemAdapterDelegate(
                analytic = analytic,
                onItemClicked = { courseListItem -> onCourseClicked(courseListItem) },
                onContinueCourseClicked = {
                    val collection = (courseCollection?.content as? CatalogBlockContent.FullCourseList) ?: return@CourseListItemAdapterDelegate
                    onCourseContinueClicked(it.course, CourseViewSource.Collection(collection.content.id), CourseContinueInteractionSource.COURSE_WIDGET)
                },
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
            initLoading(catalogBlockCourseListItem)
            courseCollection = catalogBlockCourseListItem.catalogBlockItem
            catalogBlockTitleDelegate.setInformation(catalogBlockCourseListItem.catalogBlockItem)
            catalogBlockCourseListItem
                .catalogBlockItem
                .content
                .safeCast<CatalogBlockContent.FullCourseList>()
                ?.let {
                    val countString = getCountString(it.content.coursesCount)
                    catalogBlockTitleDelegate.setCount(countString)
                }
            render(catalogBlockCourseListItem.state)
        }

        private fun render(state: CourseListFeature.State) {
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

        private fun getCountString(itemCount: Int): String =
            if (itemCount > MAX_COURSE_COUNT) {
                context.resources.getString(R.string.courses_max_count)
            } else {
                context.resources.getQuantityString(R.plurals.course_count, itemCount, itemCount)
            }
    }

    private fun initLoading(catalogBlockCourseList: CatalogBlockStateWrapper.CourseList) {
        if (catalogBlockCourseList.catalogBlockItem.content !is CatalogBlockContent.FullCourseList) {
            return
        }
        onBlockSeen(catalogBlockCourseList.id, catalogBlockCourseList.catalogBlockItem.content)
    }
}