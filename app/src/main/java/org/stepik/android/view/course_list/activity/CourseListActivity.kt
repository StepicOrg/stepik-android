package org.stepik.android.view.course_list.activity

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_course_list.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.custom.WrapContentLinearLayoutManager
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepic.droid.ui.util.setOnPaginationListener
import org.stepik.android.domain.base.PaginationDirection
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.presentation.course_list.CourseListPresenter
import org.stepik.android.view.course_list.delegate.CourseItemViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import javax.inject.Inject

class CourseListActivity : FragmentActivityBase() {

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

        val isVertical = true

        courseListPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CourseListPresenter::class.java)

        setupCourseListRecycler(courseListCoursesRecycler, isVertical = isVertical)

        courseListViewDelegate = CourseListViewDelegate(
            courseItemViewDelegate = CourseItemViewDelegate(
                activity = this,
                analytic = analytic,
                screenManager = screenManager,
                adaptiveCoursesResolver = adaptiveCoursesResolver
            ),
            adaptiveCoursesResolver = adaptiveCoursesResolver,
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

    private fun setupCourseListRecycler(courseListRecycler: RecyclerView, isVertical: Boolean) {
        with(courseListRecycler) {
            if (isVertical) {
                layoutManager = WrapContentLinearLayoutManager(context)
                setOnPaginationListener { pageDirection ->
                    if (pageDirection == PaginationDirection.DOWN) {
                        courseListPresenter.fetchNextPage()
                    }
                }
            } else {
                layoutManager = GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
                itemAnimator?.changeDuration = 0
                val snapHelper = CoursesSnapHelper(2)
                snapHelper.attachToRecyclerView(this)
                setOnPaginationListener { pageDirection ->
                    if (pageDirection == PaginationDirection.RIGHT) {
                        courseListPresenter.fetchNextPage()
                    }
                }
            }
        }
    }
}