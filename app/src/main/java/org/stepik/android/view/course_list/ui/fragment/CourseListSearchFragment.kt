package org.stepik.android.view.course_list.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_course_list.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.setOnPaginationListener
import org.stepik.android.presentation.course_list.CourseListPresenter
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class CourseListSearchFragment : Fragment() {
    companion object {
        fun newInstance(query: String): Fragment =
            CourseListSearchFragment().apply {
                this.query = query
            }
    }

    private var query by argument<String>()

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

        initCenteredToolbar(query, true)
        with(courseListCoursesRecycler) {
            addItemDecoration(org.stepik.android.view.course_list.ui.adapter.decorator.CourseListPlaceHolderTextDecoration())
            layoutManager = org.stepic.droid.ui.custom.WrapContentLinearLayoutManager(context)
            setOnPaginationListener { pageDirection ->
                if (pageDirection == org.stepik.android.domain.base.PaginationDirection.NEXT) {
                    courseListPresenter.fetchNextPage()
                }
            }
        }

        courseListViewDelegate = CourseListViewDelegate(
            courseContinueViewDelegate = CourseContinueViewDelegate(
                activity = requireActivity(),
                analytic = analytic,
                screenManager = screenManager,
                adaptiveCoursesResolver = adaptiveCoursesResolver
            ),
            adaptiveCoursesResolver = adaptiveCoursesResolver,
            courseItemsRecyclerView = courseListCoursesRecycler,
            courseListPresenter = courseListPresenter
        )

        courseListPresenter.fetchCoursesByQuery(query)
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