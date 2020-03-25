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
import org.stepic.droid.model.CollectionDescriptionColors
import org.stepic.droid.ui.custom.PlaceholderTextView
import org.stepic.droid.ui.custom.WrapContentLinearLayoutManager
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.ColorUtil
import org.stepik.android.model.CourseCollection
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.course_list.ui.adapter.decorator.CourseListPlaceHolderTextDecoration
import org.stepik.android.view.course_list.ui.decoration.HeaderDecoration
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class CourseListCollectionFragment : Fragment() {
    companion object {
        fun newInstance(courseCollection: CourseCollection): Fragment =
            CourseListCollectionFragment().apply {
                this.courseCollection = courseCollection
            }
    }

    private var courseCollection by argument<CourseCollection>()

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var adaptiveCoursesResolver: AdaptiveCoursesResolver

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var courseListViewDelegate: CourseListViewDelegate
    private lateinit var courseListPresenter: CourseListCollectionPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        courseListPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CourseListCollectionPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_course_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCenteredToolbar(courseCollection.title, true)
        with(courseListCoursesRecycler) {
            addItemDecoration(CourseListPlaceHolderTextDecoration())
            layoutManager = WrapContentLinearLayoutManager(context)
        }

        val view = PlaceholderTextView(requireContext())

        view.setPlaceholderText(R.string.are_you_sure_remove_comment_text)
        view.setBackgroundResource(CollectionDescriptionColors.FIRE.backgroundResSquared)
        view.setTextColor(ColorUtil.getColorArgb(CollectionDescriptionColors.FIRE.textColorRes, requireContext()))

        courseListCoursesRecycler.addItemDecoration(HeaderDecoration(view))

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

        courseListPresenter.fetchCourses(*courseCollection.courses)
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