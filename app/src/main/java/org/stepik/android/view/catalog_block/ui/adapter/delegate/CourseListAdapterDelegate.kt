package org.stepik.android.view.catalog_block.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.header_catalog_block.view.*
import kotlinx.android.synthetic.main.item_course_list_new.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.domain.catalog_block.model.CatalogBlock
import org.stepik.android.domain.catalog_block.model.CatalogBlockContent
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list_redux.CourseListFeature
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.catalog_block.mapper.CourseCountMapper
import org.stepik.android.view.catalog_block.model.CatalogItem
import org.stepik.android.view.catalog_block.ui.delegate.CatalogBlockHeaderDelegate
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListItemAdapterDelegate
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListPlaceHolderAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.core.model.safeCast
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseListAdapterDelegate(
    private val analytic: Analytic,
    private val courseCountMapper: CourseCountMapper,
    private val isHandleInAppPurchase: Boolean,
    private val onTitleClick: (Long) -> Unit,
    private val onBlockSeen: (String, CatalogBlockContent.FullCourseList) -> Unit,
    private val onCourseContinueClicked: (Course, CourseViewSource, CourseContinueInteractionSource) -> Unit,
    private val onCourseClicked: (CourseListItem.Data) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Block && data.catalogBlockStateWrapper is CatalogBlockStateWrapper.FullCourseList

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        CourseCollectionViewHolder(createView(parent, R.layout.item_course_list_new))

    private inner class CourseCollectionViewHolder(root: View) : DelegateViewHolder<CatalogItem>(root) {

        private var catalogBlock: CatalogBlock? = null

        private val courseListCoursesRecycler = root.courseListCoursesRecycler
        private val courseListTitleContainer = root.catalogBlockContainer

        private val catalogBlockTitleDelegate = CatalogBlockHeaderDelegate(courseListTitleContainer) {
            val block = (catalogBlock?.content as? CatalogBlockContent.FullCourseList) ?: return@CatalogBlockHeaderDelegate
            onTitleClick(block.courseList.id)
        }

        private val skeletonCount = root.resources.getInteger(R.integer.course_list_rows) * root.resources.getInteger(R.integer.course_list_columns)
        private val courseItemsSkeleton: List<CourseListItem> = List(skeletonCount) { CourseListItem.PlaceHolder() }
        private val courseItemAdapter: DefaultDelegateAdapter<CourseListItem> = DefaultDelegateAdapter()
        private val viewStateDelegate = ViewStateDelegate<CourseListFeature.State>()

        init {
            viewStateDelegate.addState<CourseListFeature.State.Idle>(courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListFeature.State.Loading>(courseListTitleContainer, courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListFeature.State.Content>(courseListTitleContainer, courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListFeature.State.Empty>()
            viewStateDelegate.addState<CourseListFeature.State.NetworkError>()

            courseItemAdapter += CourseListPlaceHolderAdapterDelegate()
            courseItemAdapter += CourseListItemAdapterDelegate(
                analytic = analytic,
                onItemClicked = { courseListItem -> onCourseClicked(courseListItem) },
                onContinueCourseClicked = {
                    val block = (catalogBlock?.content as? CatalogBlockContent.FullCourseList) ?: return@CourseListItemAdapterDelegate
                    onCourseContinueClicked(it.course, CourseViewSource.Collection(block.courseList.id), CourseContinueInteractionSource.COURSE_WIDGET)
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
            val catalogBlockCourseListItem = data.catalogBlockStateWrapper as CatalogBlockStateWrapper.FullCourseList
            initLoading(catalogBlockCourseListItem)
            catalogBlock = catalogBlockCourseListItem.catalogBlock
            catalogBlockTitleDelegate.setInformation(catalogBlockCourseListItem.catalogBlock)
            catalogBlockCourseListItem
                .catalogBlock.content
                .safeCast<CatalogBlockContent.FullCourseList>()
                ?.let {
                    val countString = courseCountMapper.mapCourseCountToString(context, it.courseList.coursesCount)
                    catalogBlockTitleDelegate.setCount(countString)
                }
            render(catalogBlockCourseListItem.state)
        }

        private fun render(state: CourseListFeature.State) {
            viewStateDelegate.switchState(state)
            when (state) {
                is CourseListFeature.State.Idle -> {
                    courseItemAdapter.items = courseItemsSkeleton
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

    private fun initLoading(catalogBlockFullCourseList: CatalogBlockStateWrapper.FullCourseList) {
        if (catalogBlockFullCourseList.catalogBlock.content !is CatalogBlockContent.FullCourseList) {
            return
        }
        onBlockSeen(catalogBlockFullCourseList.id, catalogBlockFullCourseList.catalogBlock.content)
    }
}