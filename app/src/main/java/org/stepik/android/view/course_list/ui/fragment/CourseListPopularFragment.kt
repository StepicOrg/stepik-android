package org.stepik.android.view.course_list.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_course_list.*
import kotlinx.android.synthetic.main.view_catalog_search_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.decorators.RightMarginForLastItems
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.presentation.course_list.CourseListPresenter
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class CourseListPopularFragment : Fragment() {
    companion object {
        private const val ROW_COUNT = 2

        fun newInstance(): Fragment =
            CourseListPopularFragment()
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var adaptiveCoursesResolver: AdaptiveCoursesResolver

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var courseListViewDelegate: CourseListViewDelegate
    private lateinit var courseListPresenter: CourseListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        courseListPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CourseListPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_course_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appBarLayout.isVisible = false
        courseListCoursesLoadingErrorVertical.isVisible = false
        courseListTitleContainer.isVisible = true
        courseListCoursesLoadingErrorHorizontal.isVisible = true

        courseListTitle.text = resources.getString(R.string.course_list_popular_toolbar_title)

        val iconDrawable = coursesViewAll.drawable
            .let(DrawableCompat::wrap)
            .let(Drawable::mutate)
        DrawableCompat.setTint(iconDrawable, ContextCompat.getColor(requireContext(), R.color.view_all_course_list_color_dark))

        with(courseListCoursesRecycler) {
            layoutManager = GridLayoutManager(context, ROW_COUNT, GridLayoutManager.HORIZONTAL, false)
            itemAnimator?.changeDuration = 0
            addItemDecoration(RightMarginForLastItems(resources.getDimensionPixelSize(R.dimen.home_right_recycler_padding_without_extra), ROW_COUNT))
            val snapHelper = CoursesSnapHelper(ROW_COUNT)
            snapHelper.attachToRecyclerView(this)
        }

        val courseListQuery = CourseListQuery(
            page = 1,
            order = CourseListQuery.CourseListOrder.ORDER_ACTIVITY_DESC,
            isExcludeEnded = true,
            isPublic = true
        )

        courseListTitleContainer.setOnClickListener {
            screenManager.showCoursesByQuery(
                requireContext(),
                resources.getString(R.string.course_list_popular_toolbar_title),
                courseListQuery
            )
        }

        val viewStateDelegate = ViewStateDelegate<CourseListView.State>()

        viewStateDelegate.addState<CourseListView.State.Idle>(courseListTitleContainer)
        viewStateDelegate.addState<CourseListView.State.Loading>(courseListTitleContainer, courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Content>(courseListTitleContainer, courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.NetworkError>(courseListCoursesLoadingErrorHorizontal)

        courseListViewDelegate = CourseListViewDelegate(
            courseContinueViewDelegate = CourseContinueViewDelegate(
                activity = requireActivity(),
                analytic = analytic,
                screenManager = screenManager,
                adaptiveCoursesResolver = adaptiveCoursesResolver
            ),
            adaptiveCoursesResolver = adaptiveCoursesResolver,
            courseListTitleContainer = courseListTitleContainer,
            courseItemsRecyclerView = courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            courseListPresenter = courseListPresenter
        )

        courseListPresenter.fetchCourses(courseListQuery)
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
}