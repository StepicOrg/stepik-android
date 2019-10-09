package org.stepic.droid.ui.adapters.viewhoders

import android.app.Activity
import android.graphics.drawable.Drawable
import androidx.core.text.HtmlCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import kotlinx.android.synthetic.main.new_course_item.view.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.presenters.ContinueCoursePresenter
import org.stepik.android.model.Course
import org.stepic.droid.model.CoursesCarouselColorType
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.doOnGlobalLayout
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.util.*
import org.stepik.android.view.course_list.ui.delegate.CoursePropertiesDelegate
import javax.inject.Inject

@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
        justification = "Kotlin adds null check for lateinit properties, but" +
                "Findbugs highlights it as redundant")
class CourseItemViewHolder(
        private val view: View,
        private val contextActivity: Activity,
        private val coursePlaceholder: Drawable,
        private val continueCoursePresenter: ContinueCoursePresenter,
        colorType: CoursesCarouselColorType
) : RecyclerView.ViewHolder(view) {

    @Inject
    lateinit var screenManager: ScreenManager

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var adaptiveCoursesResolver: AdaptiveCoursesResolver

    private var imageViewTarget: BitmapImageViewTarget

    private val adaptiveCourseMarker = view.adaptiveCourseMarker
    private val courseItemImage = view.courseItemImage
    private val courseContinueButton = view.courseContinueButton
    private val courseDescription = view.courseDescription
    private val courseButtonSeparator = view.courseButtonSeparator
    private val courseItemName = view.courseItemName

    private val coursePropertiesDelegate = CoursePropertiesDelegate(view.coursePropertiesContainer as ViewGroup)

    private var course: Course? = null

    init {
        App.component().inject(this)

        applyColorType(colorType)

        imageViewTarget = RoundedBitmapImageViewTarget(itemView.resources.getDimension(R.dimen.course_image_radius), courseItemImage)

        courseContinueButton.setOnClickListener { course?.let(::onClickContinueLearning) }
        courseContinueButton.setCompoundDrawables(start = R.drawable.ic_step_navigation_next)

        itemView.setOnClickListener { onClickCourse() }

        itemView.setOnLongClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            itemView.showContextMenu()
            true
        }

        view.setOnClickListener {
            course?.let {
                if (isEnrolled(it)) {
                    screenManager.showCourseModules(contextActivity, it)
                } else {
                    screenManager.showCourseDescription(contextActivity, it, false)
                }
            }
        }
    }

    private fun applyColorType(colorType: CoursesCarouselColorType) {
        (view as CardView).setCardBackgroundColor(ColorUtil.getColorArgb(colorType.backgroundColorRes, itemView.context))
        courseButtonSeparator.setBackgroundColor(ColorUtil.getColorArgb(colorType.separatorColorRes, itemView.context))
        courseItemName.setTextColor(ColorUtil.getColorArgb(colorType.textColor, itemView.context))
        coursePropertiesDelegate.setTextColor(ColorUtil.getColorArgb(colorType.textColor, itemView.context))
    }

    private fun onClickCourse() = course?.let {
        analytic.reportEvent(Analytic.Interaction.CLICK_COURSE)
        if (it.enrollment != 0L) {
            if (adaptiveCoursesResolver.isAdaptive(it.id)) {
                screenManager.continueAdaptiveCourse(contextActivity, it)
            } else {
                screenManager.showCourseModules(contextActivity, it)
            }
        } else {
            screenManager.showCourseDescription(contextActivity, it)
        }
    }

    private fun onClickContinueLearning(course: Course) {
        analytic.reportEvent(Analytic.Interaction.CLICK_CONTINUE_COURSE)
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Course.CONTINUE_PRESSED, mapOf(
                AmplitudeAnalytic.Course.Params.COURSE to course.id,
                AmplitudeAnalytic.Course.Params.SOURCE to AmplitudeAnalytic.Course.Values.COURSE_WIDGET
        ))
        continueCoursePresenter.continueCourse(course) //provide position?
    }

    fun setDataOnView(course: Course) {
        courseItemName.text = course.title
        Glide
                .with(itemView.context)
                .asBitmap()
                .load(course.cover)
                .placeholder(coursePlaceholder)
                .fitCenter()
                .into(imageViewTarget)

        courseContinueButton.changeVisibility(needShow = isEnrolled(course))
        courseButtonSeparator.changeVisibility(needShow = isEnrolled(course))
        courseDescription.changeVisibility(needShow = !isEnrolled(course))

        if (!isEnrolled(course)) {
            courseDescription.text = HtmlCompat.fromHtml(course.summary ?: "", HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
            courseDescription.doOnGlobalLayout { it.post { it.maxLines = it.height / it.lineHeight } }
        }

        coursePropertiesDelegate.setStats(course)

        adaptiveCourseMarker.changeVisibility(adaptiveCoursesResolver.isAdaptive(course.id))

        this.course = course
    }

    private fun isEnrolled(course: Course?): Boolean =
        course != null && course.enrollment != 0L
}