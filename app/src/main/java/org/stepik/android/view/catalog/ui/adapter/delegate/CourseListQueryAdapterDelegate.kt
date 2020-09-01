package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_course_list.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepic.droid.ui.util.setOnPaginationListener
import org.stepik.android.domain.base.PaginationDirection
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.PresenterViewHolder
import org.stepik.android.presentation.catalog.model.CatalogItem
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListQueryPresenter
import org.stepik.android.presentation.course_list.CourseListQueryView
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseListQueryAdapterDelegate(
    private val analytic: Analytic,
    private val screenManager: ScreenManager,
    private val courseContinueViewDelegate: CourseContinueViewDelegate,
    private val isHandleInAppPurchase: Boolean
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CourseListQueryPresenter

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        CourseQueryViewHolder(createView(parent, R.layout.item_course_list)) as DelegateViewHolder<CatalogItem>

    private inner class CourseQueryViewHolder(
        root: View
    ) : PresenterViewHolder<CourseListQueryView, CourseListQueryPresenter>(root), CourseListQueryView {

        private var courseListQuery: CourseListQuery? = null

        private val courseListCount = root.coursesCarouselCount
        private val courseListDescription = root.courseListDescription
        private val courseListCoursesRecycler = root.courseListCoursesRecycler
        private val courseListPlaceholderEmpty = root.courseListPlaceholderEmpty
        private val courseListTitleContainer = root.courseListTitleContainer
        private val courseListTitle = root.courseListTitle
        private val courseListPlaceholderNoConnection = root.courseListPlaceholderNoConnection

        private val viewStateDelegate = ViewStateDelegate<CourseListView.State>()

        init {
            courseListDescription.isVisible = false
            courseListCount.isVisible = false

            viewStateDelegate.addState<CourseListView.State.Idle>(courseListTitleContainer)
            viewStateDelegate.addState<CourseListView.State.Loading>(courseListTitleContainer, courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListView.State.Content>(courseListTitleContainer, courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListView.State.Empty>(courseListPlaceholderEmpty)
            viewStateDelegate.addState<CourseListView.State.NetworkError>(courseListPlaceholderNoConnection)

            courseListTitle.text = context.resources.getString(R.string.course_list_popular_toolbar_title)
            courseListPlaceholderEmpty.setOnClickListener { screenManager.showCatalog(itemView.context) }
            courseListPlaceholderEmpty.setPlaceholderText(R.string.empty_courses_popular)
            courseListPlaceholderNoConnection.setOnClickListener {
                val courseListQuery = courseListQuery ?: return@setOnClickListener
                itemData?.fetchCourses(courseListQuery = courseListQuery, forceUpdate = true)
            }
            courseListPlaceholderNoConnection.setText(R.string.internet_problem)

            courseListTitleContainer.setOnClickListener {
                val courseListQuery = courseListQuery ?: return@setOnClickListener
                screenManager.showCoursesByQuery(
                    itemView.context,
                    context.resources.getString(R.string.course_list_popular_toolbar_title),
                    courseListQuery
                )
            }

            with(courseListCoursesRecycler) {
                val rowCount = resources.getInteger(R.integer.course_list_rows)
                val columnsCount = resources.getInteger(R.integer.course_list_columns)
                layoutManager = TableLayoutManager(context, columnsCount, rowCount, RecyclerView.HORIZONTAL, false)
                itemAnimator?.changeDuration = 0
                val snapHelper = CoursesSnapHelper(rowCount)
                snapHelper.attachToRecyclerView(this)
                setOnPaginationListener { pageDirection ->
                    if (pageDirection == PaginationDirection.NEXT) {
                        itemData?.fetchNextPage()
                    }
                }
            }
        }

        private val delegate = CourseListViewDelegate(
            analytic = analytic,
            courseContinueViewDelegate = courseContinueViewDelegate,
            courseListTitleContainer = root.courseListTitleContainer,
            courseItemsRecyclerView = root.courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                val courseListQuery = courseListQuery ?: return@CourseListViewDelegate
                itemData
                    ?.continueCourse(
                        course = courseListItem.course,
                        viewSource = CourseViewSource.Query(courseListQuery),
                        interactionSource = CourseContinueInteractionSource.COURSE_WIDGET
                    )
            },
            isHandleInAppPurchase = isHandleInAppPurchase
        )

        override fun setState(state: CourseListQueryView.State) {
            courseListQuery = (state as? CourseListQueryView.State.Data)?.courseListQuery

            val courseListState = (state as? CourseListQueryView.State.Data)?.courseListViewState ?: CourseListView.State.Idle
            viewStateDelegate.switchState(courseListState)
            delegate.setState(courseListState)
        }

        override fun showNetworkError() {
            if (itemView.parent == null) return
            delegate.showNetworkError()
        }

        override fun showCourse(course: Course, source: CourseViewSource, isAdaptive: Boolean) {
            delegate.showCourse(course, source, isAdaptive)
        }

        override fun showSteps(course: Course, source: CourseViewSource, lastStep: LastStep) {
            delegate.showSteps(course, source, lastStep)
        }

        override fun setBlockingLoading(isLoading: Boolean) {
            delegate.setBlockingLoading(isLoading)
        }

        override fun attachView(data: CourseListQueryPresenter) {
            data.attachView(this)
            courseListCoursesRecycler.scrollToPosition(data.firstVisibleItemPosition ?: 0)
        }

        override fun detachView(data: CourseListQueryPresenter) {
            data.firstVisibleItemPosition = (courseListCoursesRecycler.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
            data.detachView(this)
        }
    }
}