package org.stepik.android.view.course_list.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.empty_search.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.fragment_course_list.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.custom.WrapContentLinearLayoutManager
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.setOnPaginationListener
import org.stepik.android.domain.base.PaginationDirection
import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.model.Tag
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListSearchPresenter
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class CourseListTagFragment : Fragment() {
    companion object {
        fun newInstance(tag: Tag): Fragment =
            CourseListTagFragment().apply {
                this.tag = tag
            }
    }

    private var tag by argument<Tag>()

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var courseListViewDelegate: CourseListViewDelegate
    private lateinit var courseListPresenter: CourseListSearchPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        courseListPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CourseListSearchPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_course_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCenteredToolbar(tag.title, true)
        with(courseListCoursesRecycler) {
            layoutManager = WrapContentLinearLayoutManager(context)
            setOnPaginationListener { pageDirection ->
                if (pageDirection == PaginationDirection.NEXT) {
                    courseListPresenter.fetchNextPage()
                }
            }
        }

        val viewStateDelegate = ViewStateDelegate<CourseListView.State>()
        viewStateDelegate.addState<CourseListView.State.Idle>()
        viewStateDelegate.addState<CourseListView.State.Loading>(courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Content>(courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Empty>(courseListCoursesEmpty)
        viewStateDelegate.addState<CourseListView.State.NetworkError>(courseListCoursesLoadingErrorVertical)

        courseListViewDelegate = CourseListViewDelegate(
            courseContinueViewDelegate = CourseContinueViewDelegate(
                activity = requireActivity(),
                analytic = analytic,
                screenManager = screenManager
            ),
            courseListTitleContainer = courseListTitleContainer,
            courseListSwipeRefresh = courseListSwipeRefresh,
            courseItemsRecyclerView = courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                courseListPresenter.continueCourse(course = courseListItem.course, interactionSource = CourseContinueInteractionSource.COURSE_WIDGET)
            }
        )

        goToCatalog.setOnClickListener { screenManager.showCatalog(requireContext()) }
        courseListSwipeRefresh.setOnRefreshListener {
            courseListPresenter.fetchCourses(
                SearchResultQuery(
                    page = 1,
                    tagId = tag.id
                ),
                forceUpdate = true
            )
        }
        tryAgain.setOnClickListener {
            courseListPresenter.fetchCourses(
                SearchResultQuery(
                    page = 1,
                    tagId = tag.id
                ),
                forceUpdate = true
            )
        }

        courseListPresenter.fetchCourses(
            SearchResultQuery(
                page = 1,
                tagId = tag.id
            )
        )
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