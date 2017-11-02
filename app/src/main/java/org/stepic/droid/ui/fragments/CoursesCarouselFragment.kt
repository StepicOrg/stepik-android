package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_courses_carousel.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.Client
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.dropping.contract.DroppingListener
import org.stepic.droid.core.joining.contract.JoiningListener
import org.stepic.droid.core.presenters.ContinueCoursePresenter
import org.stepic.droid.core.presenters.DroppingPresenter
import org.stepic.droid.core.presenters.PersistentCourseListPresenter
import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.core.presenters.contracts.DroppingView
import org.stepic.droid.model.Course
import org.stepic.droid.model.CoursesCarouselInfo
import org.stepic.droid.model.Section
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.adapters.CoursesAdapter
import org.stepic.droid.ui.decorators.LeftSpacesDecoration
import org.stepic.droid.ui.decorators.RightMarginForLastItems
import org.stepic.droid.ui.decorators.VerticalSpacesInGridDecoration
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.StartSnapHelper
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.StepikUtil
import org.stepic.droid.util.SuppressFBWarnings
import javax.inject.Inject

@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "Kotlin adds null check for lateinit properties, but Findbugs highlights it as redundant")
class CoursesCarouselFragment
    : FragmentBase(),
        ContinueCourseView,
        CoursesView,
        DroppingView,
        JoiningListener,
        DroppingListener {
    companion object {
        private const val COURSE_CAROUSEL_INFO_KEY = "COURSE_CAROUSEL_INFO_KEY"

        fun newInstance(coursesCarouselInfo: CoursesCarouselInfo): CoursesCarouselFragment {
            val args = Bundle()
            val fragment = CoursesCarouselFragment()
            args.putParcelable(COURSE_CAROUSEL_INFO_KEY, coursesCarouselInfo)
            fragment.arguments = args
            return fragment
        }

        //FIXME: 04.09.17 if adapter.count < ROW_COUNT -> recycler creates extra padding
        private const val ROW_COUNT = 2

        private const val continueLoadingTag = "continueLoadingTag"
    }

    @Inject
    lateinit var courseListPresenter: PersistentCourseListPresenter

    @Inject
    lateinit var droppingPresenter: DroppingPresenter

    @Inject
    lateinit var continueCoursePresenter: ContinueCoursePresenter

    @Inject
    lateinit var droppingClient: Client<DroppingListener>

    @Inject
    lateinit var joiningListenerClient: Client<JoiningListener>

    private val courses = ArrayList<Course>()
    private lateinit var info: CoursesCarouselInfo
    private var gridLayoutManager: GridLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        info = arguments.getParcelable(COURSE_CAROUSEL_INFO_KEY)
    }

    override fun injectComponent() {
        App
                .componentManager()
                .courseGeneralComponent()
                .courseListComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onReleaseComponent() {
        App
                .componentManager()
                .releaseCourseGeneralComponent()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.fragment_courses_carousel, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCourseCarousel()
        continueCoursePresenter.attachView(this)
        courseListPresenter.attachView(this)
        droppingPresenter.attachView(this)
        droppingClient.subscribe(this)
        joiningListenerClient.subscribe(this)

        restoreState()

        courses.clear()
        downloadData()
    }

    override fun onPause() {
        super.onPause()
        ProgressHelper.dismiss(fragmentManager, continueLoadingTag)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        joiningListenerClient.unsubscribe(this)
        droppingClient.unsubscribe(this)
        continueCoursePresenter.detachView(this)
        courseListPresenter.detachView(this)
        droppingPresenter.detachView(this)
    }

    private fun initCourseCarousel() {
        coursesCarouselTitle.text = getCarouselTitle()
        coursesCarouselTitle.setTextColor(ColorUtil.getColorArgb(info.colorType.textColor))

        coursesCarouselRoot.setBackgroundColor(ColorUtil.getColorArgb(info.colorType.backgroundColorRes))

        coursesViewAll.setOnClickListener {
            viewAll()
        }
        coursesViewAll.setTextColor(ColorUtil.getColorArgb(info.colorType.viewAllColorRes, context))

        gridLayoutManager = GridLayoutManager(context, ROW_COUNT, GridLayoutManager.HORIZONTAL, false)
        coursesRecycler.layoutManager = gridLayoutManager
        val showMore = info.table == Table.enrolled
        coursesRecycler.adapter = CoursesAdapter(this, courses, continueCoursePresenter, droppingPresenter, false, showMore, info.colorType)
        val verticalSpaceBetweenItems = resources.getDimensionPixelSize(R.dimen.course_list_between_items_padding)
        val leftSpacePx = resources.getDimensionPixelSize(R.dimen.course_list_side_padding)
        coursesRecycler.addItemDecoration(VerticalSpacesInGridDecoration(verticalSpaceBetweenItems / 2, ROW_COUNT)) //warning: verticalSpaceBetweenItems/2 – workaround for some bug, decoration will set this param twice
        coursesRecycler.addItemDecoration(LeftSpacesDecoration(leftSpacePx))
        coursesRecycler.addItemDecoration(RightMarginForLastItems(resources.getDimensionPixelSize(R.dimen.home_right_recycler_padding_without_extra), ROW_COUNT))
        coursesRecycler.itemAnimator.changeDuration = 0
        val snapHelper = StartSnapHelper()
        snapHelper.attachToRecyclerView(coursesRecycler)
    }

    override fun onOpenStep(courseId: Long, section: Section, lessonId: Long, unitId: Long, stepPosition: Int) {
        ProgressHelper.dismiss(fragmentManager, continueLoadingTag)
        screenManager.continueCourse(activity, courseId, section, lessonId, unitId, stepPosition.toLong())
    }

    override fun onAnyProblemWhileContinue(course: Course) {
        ProgressHelper.dismiss(fragmentManager, continueLoadingTag)
        screenManager.showSections(activity, course)
    }

    override fun onShowContinueCourseLoadingDialog() {
        val loadingProgressDialogFragment = LoadingProgressDialogFragment.newInstance()
        if (!loadingProgressDialogFragment.isAdded) {
            loadingProgressDialogFragment.show(fragmentManager, continueLoadingTag)
        }
    }

    override fun showLoading() {
        coursesViewAll.visibility = View.GONE
        coursesRecycler.visibility = View.GONE
        coursesPlaceholder.visibility = View.GONE
        coursesLoadingView.visibility = View.VISIBLE
    }

    override fun showEmptyCourses() {
        @StringRes
        fun getEmptyStringRes(table: Table?): Int =
                when (table) {
                    Table.enrolled -> {
                        R.string.courses_carousel_my_courses_empty
                    }
                    Table.featured -> {
                        analytic.reportEvent(Analytic.Error.FEATURED_EMPTY)
                        R.string.empty_courses_popular
                    }
                    else -> {
                        // TODO: 29.09.2017 implement for course list
                        TODO()
                    }
                }



        if (info.table == Table.enrolled) {
            analytic.reportEvent(Analytic.CoursesCarousel.EMPTY_ENROLLED_SHOWN)
        }
        showPlaceholder(getEmptyStringRes(info.table), {
            if (info.table == Table.enrolled) {
                analytic.reportEvent(Analytic.CoursesCarousel.EMPTY_ENROLLED_CLICK)
                screenManager.showFindCourses(context)
            }
        })
    }

    override fun showConnectionProblem() {
        if (courses.isEmpty()) {
            analytic.reportEvent(Analytic.CoursesCarousel.NO_INTERNET_SHOWN)
            showPlaceholder(R.string.internet_problem, { _ ->
                analytic.reportEvent(Analytic.CoursesCarousel.NO_INTERNET_CLICK)
                if (StepikUtil.isInternetAvailable()) {
                    downloadData()
                }
            })
        }
    }

    override fun showCourses(courses: MutableList<Course>) {
        coursesLoadingView.visibility = View.GONE
        coursesPlaceholder.visibility = View.GONE
        coursesRecycler.visibility = View.VISIBLE
        coursesViewAll.visibility = View.VISIBLE
        this.courses.clear()
        this.courses.addAll(courses)
        coursesRecycler.adapter.notifyDataSetChanged()
        updateOnCourseCountChanged()
    }

    private fun showPlaceholder(@StringRes stringRes: Int, listener: (view: View) -> Unit) {
        coursesViewAll.visibility = View.GONE
        coursesLoadingView.visibility = View.GONE
        coursesRecycler.visibility = View.GONE
        coursesPlaceholder.setPlaceholderText(stringRes)
        coursesPlaceholder.setOnClickListener(listener)
        coursesPlaceholder.visibility = View.VISIBLE
    }

    override fun onUserHasNotPermissionsToDrop() {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onSuccessDropCourse(course: Course) {
        val courseId = course.courseId
        analytic.reportEvent(Analytic.Web.DROP_COURSE_SUCCESSFUL, courseId.toString())
        Toast.makeText(context, context.getString(R.string.you_dropped, course.title), Toast.LENGTH_LONG).show()
        val index = courses.indexOfFirst { it.courseId == course.courseId }

        if (index < 0) {
            //course is not in list
            return
        }

        if (info.table == Table.enrolled) {
            courses.removeAt(index)
            coursesRecycler.adapter.notifyItemRemoved(index)
            if (courses.size == ROW_COUNT) {
                // update 1st column for adjusting size
                coursesRecycler.adapter.notifyItemRangeChanged(0, ROW_COUNT - 1) // "ROW_COUNT - 1" count is number of changed items, we shouldn't update the last item
            }
            updateOnCourseCountChanged()
        } else {
            courses[index].enrollment = 0
            coursesRecycler.adapter.notifyItemChanged(index)
        }

        if (courses.isEmpty()) {
            showEmptyCourses()
        }
    }

    override fun onFailDropCourse(course: Course) {
        val courseId = course.courseId
        analytic.reportEvent(Analytic.Web.DROP_COURSE_FAIL, courseId.toString())
        Toast.makeText(context, R.string.internet_problem, Toast.LENGTH_LONG).show()

    }

    private fun getCarouselTitle(): String = info.title

    private fun restoreState() {
        if (info.table != null) {
            courseListPresenter.restoreState()
        } else if (info.table == null) {
            // no-op
        }
    }

    private fun downloadData() {
        info.table?.let {
            courseListPresenter.refreshData(it, false, false)
        }

        if (info.table == null) {
            // TODO: 26.09.2017 implement course list fetching (presenter and view : CoursesView)
        }
    }

    private fun viewAll() {
        screenManager.showCoursesList(activity, info)
    }

    override fun onSuccessJoin(joinedCourse: Course) {
        val courseIndex = courses.indexOfFirst { it.courseId == joinedCourse.courseId }

        if (courseIndex >= 0) {
            courses[courseIndex].enrollment = joinedCourse.enrollment
            coursesRecycler.adapter.notifyItemChanged(courseIndex)
        } else if (info.table == Table.enrolled) {
            //insert at 0 index is more complex than just add, but order will be right
            if (courses.isEmpty()) {
                showCourses(mutableListOf(joinedCourse))
            } else {
                courses.add(0, joinedCourse)
                coursesRecycler.adapter.notifyDataSetChanged()
                updateOnCourseCountChanged()
            }
        }
    }

    private fun updateOnCourseCountChanged() {
        updateSpanCount()
        updateCourseCount()
    }

    private fun updateSpanCount() {
        if (courses.isEmpty()) {
            //do nothing
            return
        }

        val spanCount = Math.min(courses.size, ROW_COUNT)
        gridLayoutManager?.spanCount = spanCount
    }

    private fun updateCourseCount() {
        if (info.table == Table.featured || courses.isEmpty()) {
            coursesCarouselCount.visibility = View.GONE
        } else {
            coursesCarouselCount.visibility = View.VISIBLE
            coursesCarouselCount.text = resources.getQuantityString(R.plurals.course_count, courses.size, courses.size)
        }
    }


}
