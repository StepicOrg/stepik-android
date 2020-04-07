package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_course_list.view.*
import kotlinx.android.synthetic.main.view_catalog_search_toolbar.view.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.decorators.RightMarginForLastItems
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.presentation.base.PresenterViewHolder
import org.stepik.android.presentation.catalog.CatalogItem
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseListAdapterDelegate(
    private val screenManager: ScreenManager,
    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val courseContinueViewDelegate: CourseContinueViewDelegate
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CourseListCollectionPresenter

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        CourseCollectionViewHolder(createView(parent, R.layout.fragment_course_list)) as DelegateViewHolder<CatalogItem>

    private inner class CourseCollectionViewHolder(
        root: View
    ) : PresenterViewHolder<CourseListView, CourseListCollectionPresenter>(root) {

        private val appBarLayout = root.appBarLayout
        private val courseListCoursesLoadingErrorVertical = root.courseListCoursesLoadingErrorVertical
        private val coursesCarouselCount = root.coursesCarouselCount
        private val courseListCoursesRecycler = root.courseListCoursesRecycler
        private val courseListPlaceholderEmpty = root.courseListPlaceholderEmpty
        private val courseListTitleContainer = root.courseListTitleContainer
        private val courseListPlaceholderNoConnection = root.courseListPlaceholderNoConnection

        private val viewStateDelegate = ViewStateDelegate<CourseListView.State>()

        init {
            appBarLayout.isVisible = false
            courseListCoursesLoadingErrorVertical.isVisible = false
            courseListTitleContainer.isVisible = true
            coursesCarouselCount.isVisible = true
            courseListPlaceholderEmpty.isVisible = true
            courseListPlaceholderNoConnection.isVisible = true

            viewStateDelegate.addState<CourseListView.State.Idle>(courseListTitleContainer)
            viewStateDelegate.addState<CourseListView.State.Loading>(courseListTitleContainer, courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListView.State.Content>(courseListTitleContainer, courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListView.State.Empty>(courseListPlaceholderEmpty)
            viewStateDelegate.addState<CourseListView.State.NetworkError>(courseListPlaceholderNoConnection)

            courseListPlaceholderEmpty.setOnClickListener { screenManager.showCatalog(itemView.context) }
            courseListPlaceholderEmpty.setPlaceholderText(R.string.empty_courses_popular)
            courseListPlaceholderNoConnection.setOnClickListener {
                // TODO
                // courseListPresenter.fetchCourses(courseListQuery, forceUpdate = true)
            }
            courseListPlaceholderNoConnection.setText(R.string.internet_problem)

            with(courseListCoursesRecycler) {
                layoutManager = GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
                itemAnimator?.changeDuration = 0
                addItemDecoration(RightMarginForLastItems(resources.getDimensionPixelSize(R.dimen.new_home_right_recycler_padding_without_extra), 2))
                val snapHelper = CoursesSnapHelper(2)
                snapHelper.attachToRecyclerView(this)
            }
        }

        private val delegate = CourseListViewDelegate(
            courseContinueViewDelegate = courseContinueViewDelegate,
            adaptiveCoursesResolver = adaptiveCoursesResolver,
            courseListTitleContainer = root.courseListTitleContainer,
            courseItemsRecyclerView = root.courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                itemData?.continueCourse(course = courseListItem.course, interactionSource = CourseContinueInteractionSource.COURSE_WIDGET)
            }
        )

        override fun attachView(data: CourseListCollectionPresenter) {
            data.attachView(delegate)
        }

        override fun detachView(data: CourseListCollectionPresenter) {
            data.detachView(delegate)
        }
    }
}