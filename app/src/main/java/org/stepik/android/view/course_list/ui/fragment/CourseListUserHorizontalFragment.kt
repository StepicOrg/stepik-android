package org.stepik.android.view.course_list.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_course_list.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.decorators.RightMarginForLastItems
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListUserPresenter
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class CourseListUserHorizontalFragment : Fragment() {
    companion object {
        private const val ROW_COUNT = 2

        fun newInstance(): Fragment =
            CourseListUserHorizontalFragment()
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var courseListViewDelegate: CourseListViewDelegate
    private lateinit var courseListPresenter: CourseListUserPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        courseListPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CourseListUserPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.item_course_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        courseListTitle.text = resources.getString(R.string.course_list_user_courses_title)

        val iconDrawable = coursesViewAll.drawable
            .let(DrawableCompat::wrap)
            .let(Drawable::mutate)
        DrawableCompat.setTint(iconDrawable, ContextCompat.getColor(requireContext(), R.color.view_all_course_list_color_dark))

        val courseListPadding = resources.getDimensionPixelOffset(R.dimen.course_list_padding)
        courseListCoursesRecycler.setPadding(
            courseListPadding,
            courseListPadding,
            resources.getDimensionPixelSize(R.dimen.home_right_recycler_padding),
            courseListPadding
        )

        with(courseListCoursesRecycler) {
            layoutManager = GridLayoutManager(context, ROW_COUNT, GridLayoutManager.HORIZONTAL, false)
            itemAnimator?.changeDuration = 0
            addItemDecoration(RightMarginForLastItems(resources.getDimensionPixelSize(R.dimen.new_home_right_recycler_padding_without_extra), ROW_COUNT))
            val snapHelper = CoursesSnapHelper(ROW_COUNT)
            snapHelper.attachToRecyclerView(this)
        }

        courseListTitleContainer.setOnClickListener { screenManager.showUserCourses(requireContext()) }
        courseListPlaceholderEmpty.setOnClickListener { screenManager.showCatalog(requireContext()) }
        courseListPlaceholderEmpty.setPlaceholderText(R.string.empty_courses_popular)
        courseListPlaceholderNoConnection.setOnClickListener { courseListPresenter.fetchCourses(forceUpdate = true) }
        courseListPlaceholderNoConnection.setText(R.string.internet_problem)

        val viewStateDelegate = ViewStateDelegate<CourseListView.State>()

        viewStateDelegate.addState<CourseListView.State.Idle>(courseListTitleContainer)
        viewStateDelegate.addState<CourseListView.State.Loading>(courseListTitleContainer, courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Content>(courseListTitleContainer, courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Empty>(courseListPlaceholderEmpty)
        viewStateDelegate.addState<CourseListView.State.NetworkError>(courseListPlaceholderNoConnection)

        courseListViewDelegate = CourseListViewDelegate(
            courseContinueViewDelegate = CourseContinueViewDelegate(
                activity = requireActivity(),
                analytic = analytic,
                screenManager = screenManager
            ),
            courseListTitleContainer = courseListTitleContainer,
            courseItemsRecyclerView = courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                courseListPresenter.continueCourse(course = courseListItem.course, interactionSource = CourseContinueInteractionSource.COURSE_WIDGET)
            }
        )

        courseListPresenter.fetchCourses()
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