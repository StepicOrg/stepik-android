package org.stepik.android.view.course_list.activity

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_course_list.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.decorators.RightMarginForLastItems
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_list.CourseListPresenter
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import javax.inject.Inject

class CourseListActivity : FragmentActivityBase(), CourseContinueView {

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    @Inject
    internal lateinit var adaptiveCoursesResolver: AdaptiveCoursesResolver

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var courseListViewDelegate: CourseListViewDelegate
    private lateinit var courseListPresenter: CourseListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list)
        injectComponent()

        courseListPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CourseListPresenter::class.java)

        with(courseListCoursesRecycler) {
            // Vertical
//            layoutManager = WrapContentLinearLayoutManager(context)
//            setOnPaginationListener { pageDirection ->
//                if (pageDirection == PaginationDirection.DOWN) {
//                    courseListPresenter.fetchNextPage()
//                }
//            }

            // Horizontal
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
            itemAnimator?.changeDuration = 0
            addItemDecoration(RightMarginForLastItems(resources.getDimensionPixelSize(R.dimen.home_right_recycler_padding_without_extra), 2))
            val snapHelper = CoursesSnapHelper(2)
            snapHelper.attachToRecyclerView(this)

            // TODO Could be extension?
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = (recyclerView.layoutManager as? LinearLayoutManager)
                        ?: return

                    val pastVisibleItems = layoutManager.findFirstCompletelyVisibleItemPosition()

                    if (dx > 0) {
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount

                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            post { courseListPresenter.fetchNextPage() }
                        }
                    }
                }
            })
        }

        courseListViewDelegate = CourseListViewDelegate(
            courseContinueView = this,
            onCourseClicked = ::onCourseClicked,
            adaptiveCoursesResolver = adaptiveCoursesResolver,
            isVertical = false,
            courseItemsRecyclerView = courseListCoursesRecycler,
            courseListPresenter = courseListPresenter
        )

        courseListPresenter.fetchCourses(courseListQuery = CourseListQuery(page = 1, order = CourseListQuery.ORDER_ACTIVITY_DESC, teacher = 651763))
    }

    private fun injectComponent() {
        App.component()
            .courseListExperimentalComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        courseListPresenter.attachView(courseListViewDelegate)
    }

    override fun onStop() {
        courseListPresenter.detachView(courseListViewDelegate)
        super.onStop()
    }

    override fun setBlockingLoading(isLoading: Boolean) {
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }

    override fun showCourse(course: Course, isAdaptive: Boolean) {
        if (isAdaptive) {
            screenManager.continueAdaptiveCourse(this, course)
        } else {
            screenManager.showCourseModules(this, course)
        }
    }

    override fun showSteps(course: Course, lastStep: LastStep) {
        screenManager.continueCourse(this, course.id, lastStep)
    }

    private fun onCourseClicked(courseListItem: CourseListItem.Data) {
        analytic.reportEvent(Analytic.Interaction.CLICK_COURSE)
        if (courseListItem.course.enrollment != 0L) {
            if (adaptiveCoursesResolver.isAdaptive(courseListItem.id)) {
                screenManager.continueAdaptiveCourse(this, courseListItem.course)
            } else {
                screenManager.showCourseModules(this, courseListItem.course)
            }
        } else {
            screenManager.showCourseDescription(this, courseListItem.course)
        }
    }
}