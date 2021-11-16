package org.stepik.android.view.course_list.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import kotlinx.android.synthetic.main.fragment_course_list.*
import kotlinx.android.synthetic.main.fragment_course_list.courseListCoursesRecycler
import kotlinx.android.synthetic.main.item_course_list.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_payments.mapper.DefaultPromoCodeMapper
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.presentation.course_list.CourseListVisitedPresenter
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class CourseListVisitedFragment : Fragment(R.layout.fragment_course_list) {
    companion object {
        fun newInstance(): Fragment =
            CourseListVisitedFragment()

        private const val PURCHASE_FLOW_IAP = "iap"
        private const val PURCHASE_FLOW_WEB = "web"
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    internal lateinit var defaultPromoCodeMapper: DefaultPromoCodeMapper

    @Inject
    internal lateinit var displayPriceMapper: DisplayPriceMapper

    @Inject
    lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    private lateinit var courseListViewDelegate: CourseListViewDelegate
    private val courseListVisitedPresenter: CourseListVisitedPresenter by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCenteredToolbar(R.string.visited_courses_title, true)

        courseListSwipeRefresh.isEnabled = false
        courseListCoursesRecycler.layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.course_list_columns))

        val viewStateDelegate = ViewStateDelegate<CourseListView.State>()
        viewStateDelegate.addState<CourseListView.State.Idle>()
        viewStateDelegate.addState<CourseListView.State.Loading>(courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Content>(courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Empty>(courseListCoursesEmpty)
        viewStateDelegate.addState<CourseListView.State.NetworkError>(courseListCoursesLoadingErrorVertical)

        courseListViewDelegate = CourseListViewDelegate(
            analytic = analytic,
            courseContinueViewDelegate = CourseContinueViewDelegate(
                activity = requireActivity(),
                analytic = analytic,
                screenManager = screenManager
            ),
            courseItemsRecyclerView = courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                courseListVisitedPresenter
                    .continueCourse(
                        course = courseListItem.course,
                        viewSource = CourseViewSource.Visited,
                        interactionSource = CourseContinueInteractionSource.COURSE_WIDGET
                    )
            },
            defaultPromoCodeMapper = defaultPromoCodeMapper,
            displayPriceMapper = displayPriceMapper,
            isIAPFlowEnabled = firebaseRemoteConfig[RemoteConfig.PURCHASE_FLOW_ANDROID].asString() == PURCHASE_FLOW_IAP,
            itemAdapterDelegateType = CourseListViewDelegate.ItemAdapterDelegateType.STANDARD
        )

        courseListVisitedPresenter.fetchCourses()
    }

    private fun injectComponent() {
        App.component()
            .courseListVisitedComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        courseListVisitedPresenter.attachView(courseListViewDelegate)
    }

    override fun onStop() {
        courseListVisitedPresenter.detachView(courseListViewDelegate)
        super.onStop()
    }
}