package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.item_course_list.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.model.CollectionDescriptionColors
import org.stepic.droid.ui.decorators.RightMarginForLastItems
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.model.CourseCollection
import org.stepik.android.presentation.base.PresenterViewHolder
import org.stepik.android.presentation.catalog.model.CatalogItem
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.course_list.CourseListCollectionView
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseListAdapterDelegate(
    private val analytic: Analytic,
    private val screenManager: ScreenManager,
    private val courseContinueViewDelegate: CourseContinueViewDelegate
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    companion object {
        private const val ROW_COUNT = 2
    }

    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CourseListCollectionPresenter

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        CourseCollectionViewHolder(createView(parent, R.layout.item_course_list)) as DelegateViewHolder<CatalogItem>

    private inner class CourseCollectionViewHolder(
        root: View
    ) : PresenterViewHolder<CourseListCollectionView, CourseListCollectionPresenter>(root), CourseListCollectionView {

        private var courseCollection: CourseCollection? = null

        private val courseListTitle = root.courseListTitle
        private val courseListDescription = root.courseListDescription
        private val courseListCoursesRecycler = root.courseListCoursesRecycler
        private val courseListPlaceholderEmpty = root.courseListPlaceholderEmpty
        private val courseListTitleContainer = root.courseListTitleContainer
        private val courseListPlaceholderNoConnection = root.courseListPlaceholderNoConnection

        private val viewStateDelegate = ViewStateDelegate<CourseListView.State>()

        init {
            viewStateDelegate.addState<CourseListView.State.Idle>(courseListTitleContainer, courseListDescription)
            viewStateDelegate.addState<CourseListView.State.Loading>(courseListTitleContainer, courseListDescription, courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListView.State.Content>(courseListTitleContainer, courseListDescription, courseListCoursesRecycler)
            viewStateDelegate.addState<CourseListView.State.Empty>(courseListPlaceholderEmpty)
            viewStateDelegate.addState<CourseListView.State.NetworkError>(courseListPlaceholderNoConnection)

            val onClickListener = View.OnClickListener {
                val collection = courseCollection ?: return@OnClickListener

                val collectionColors =
                    if (adapterPosition % 2 == 0) {
                        CollectionDescriptionColors.BLUE
                    } else {
                        CollectionDescriptionColors.FIRE
                    }
                screenManager.showCoursesCollection(itemView.context, collection, collectionColors)
            }

            courseListDescription.setOnClickListener(onClickListener)
            courseListTitleContainer.setOnClickListener(onClickListener)

            courseListPlaceholderEmpty.setOnClickListener { screenManager.showCatalog(itemView.context) }
            courseListPlaceholderEmpty.setPlaceholderText(R.string.empty_courses_popular)
            courseListPlaceholderNoConnection.setOnClickListener {
                val collection = courseCollection ?: return@setOnClickListener
                itemData?.fetchCourses(courseCollection = collection, forceUpdate = true)
            }
            courseListPlaceholderNoConnection.setText(R.string.internet_problem)

            with(courseListCoursesRecycler) {
                layoutManager = GridLayoutManager(context, ROW_COUNT, GridLayoutManager.HORIZONTAL, false)
                itemAnimator?.changeDuration = 0
                addItemDecoration(RightMarginForLastItems(resources.getDimensionPixelSize(R.dimen.new_home_right_recycler_padding_without_extra), ROW_COUNT))
                val snapHelper = CoursesSnapHelper(ROW_COUNT)
                snapHelper.attachToRecyclerView(this)
            }
        }

        private val delegate = CourseListViewDelegate(
            courseContinueViewDelegate = courseContinueViewDelegate,
            courseListTitleContainer = root.courseListTitleContainer,
            courseItemsRecyclerView = root.courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                val courseCollectionId = this.courseCollection?.id
                    ?: return@CourseListViewDelegate

                itemData
                    ?.continueCourse(
                        course = courseListItem.course,
                        viewSource = CourseViewSource.Collection(courseCollectionId),
                        interactionSource = CourseContinueInteractionSource.COURSE_WIDGET
                    )
            }
        )

        override fun setState(state: CourseListCollectionView.State) {
            courseCollection = (state as? CourseListCollectionView.State.Data)?.courseCollection
            courseCollection?.let {
                courseListTitle.text = it.title
                courseListDescription.setPlaceholderText(it.description)
            }

            val courseListState = (state as? CourseListCollectionView.State.Data)?.courseListViewState ?: CourseListView.State.Idle
            if (courseListState == CourseListView.State.Empty) {
                analytic.reportEvent(Analytic.Error.COURSE_COLLECTION_EMPTY)
            }
            viewStateDelegate.switchState(courseListState)
            delegate.setState(courseListState)
        }

        override fun showNetworkError() {
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

        override fun attachView(data: CourseListCollectionPresenter) {
            data.attachView(this)

            val collectionColors =
                if (adapterPosition % 2 == 0) {
                    CollectionDescriptionColors.BLUE
                } else {
                    CollectionDescriptionColors.FIRE
                }

            with(collectionColors) {
                courseListDescription.setBackgroundResource(backgroundRes)
                courseListDescription.setTextColor(AppCompatResources.getColorStateList(context, textColorRes))
            }

            courseListCoursesRecycler.scrollToPosition(data.firstVisibleItemPosition ?: 0)
        }

        override fun detachView(data: CourseListCollectionPresenter) {
            data.firstVisibleItemPosition = (courseListCoursesRecycler.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
            data.detachView(this)
        }
    }
}