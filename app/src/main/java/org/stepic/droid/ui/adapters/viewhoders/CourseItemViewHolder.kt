package org.stepic.droid.ui.adapters.viewhoders

import android.app.Activity
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import kotlinx.android.synthetic.main.new_course_item.view.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.configuration.Config
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.presenters.ContinueCoursePresenter
import org.stepic.droid.core.presenters.DroppingPresenter
import org.stepik.android.model.Course
import org.stepic.droid.model.CoursesCarouselColorType
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.*
import java.util.*
import javax.inject.Inject

@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
        justification = "Kotlin adds null check for lateinit properties, but" +
                "Findbugs highlights it as redundant")
class CourseItemViewHolder(
        val view: View,
        private val contextActivity: Activity,
        private val showMore: Boolean,
        private val joinTitle: String,
        private val continueTitle: String,
        private val coursePlaceholder: Drawable,
        private val droppingPresenter: DroppingPresenter,
        private val continueCoursePresenter: ContinueCoursePresenter,
        private val colorType: CoursesCarouselColorType
) : RecyclerView.ViewHolder(view) {

    @Inject
    lateinit var screenManager: ScreenManager

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var adaptiveCoursesResolver: AdaptiveCoursesResolver

    private val continueColor: Int by lazy {
        ColorUtil.getColorArgb(colorType.textColor, itemView.context)
    }
    private val joinColor: Int by lazy {
        ColorUtil.getColorArgb(R.color.join_text_color, itemView.context)
    }
    private val infoTitle: String by lazy {
        itemView.context.resources.getString(R.string.course_item_info)
    }
    private var imageViewTarget: BitmapImageViewTarget

    private val adaptiveCourseMarker = view.adaptiveCourseMarker
    private val courseItemImage = view.courseItemImage
    private val courseWidgetButton = view.courseWidgetButton
    private val courseItemName = view.courseItemName
    private val learnersCountImage = view.learnersCountImage
    private val learnersCountText = view.learnersCountText
    private val courseItemMore = view.courseItemMore
    private val coursePropertiesContainer = view.coursePropertiesContainer
    private val courseWidgetInfo = view.courseWidgetInfo
    private val courseItemProgress = view.courseItemProgressView
    private val courseItemProgressTitle = view.courseItemProgressTitle
    private val courseRatingImage = view.courseRatingImage
    private val courseRatingText = view.courseRatingText

    private var course: Course? = null

    init {
        App.component().inject(this)

        applyColorType(colorType)

        imageViewTarget = RoundedBitmapImageViewTarget(itemView.resources.getDimension(R.dimen.course_image_radius), courseItemImage)

        courseWidgetButton.setOnClickListener {
            course?.let {
                onClickWidgetButton(it, isEnrolled(it))
            }
        }
        itemView.setOnClickListener { onClickCourse() }

        itemView.setOnLongClickListener({ v ->
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            itemView.showContextMenu()
            true
        })

        itemView.courseItemMore.setOnClickListener { view ->
            course?.let {
                showMore(view, it)
            }
        }

        courseWidgetInfo.applyToButton(infoTitle, continueColor, colorType.continueResource)
        courseWidgetInfo.setOnClickListener {
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
        courseItemName.setTextColor(ColorUtil.getColorArgb(colorType.textColor, itemView.context))
        learnersCountText.setTextColor(ColorUtil.getColorArgb(colorType.textColor, itemView.context))
        learnersCountImage.setColorFilter(ColorUtil.getColorArgb(colorType.textColor, itemView.context))
        courseWidgetButton.setTextColor(ColorUtil.getColorArgb(colorType.textColor, itemView.context))
        courseRatingText.setTextColor(ColorUtil.getColorArgb(colorType.textColor, itemView.context))
        courseRatingImage.setColorFilter(ColorUtil.getColorArgb(colorType.textColor, itemView.context))
        courseItemProgress.backgroundPaintColor = ColorUtil.getColorArgb(colorType.textColor, itemView.context)
    }

    private fun showMore(view: View, course: Course) {
        val morePopupMenu = PopupMenu(itemView.context, view)
        morePopupMenu.inflate(ContextMenuCourseUtil.getMenuResource(course))

        morePopupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_info -> {
                    screenManager.showCourseDescription(itemView.context, course)
                    true
                }
                R.id.menu_item_unroll -> {
                    droppingPresenter.dropCourse(course)
                    true
                }
                else -> false
            }
        }
        morePopupMenu.show()
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

    private fun onClickWidgetButton(course: Course, enrolled: Boolean) {
        if (enrolled) {
            analytic.reportEvent(Analytic.Interaction.CLICK_CONTINUE_COURSE)
            analytic.reportAmplitudeEvent(AmplitudeAnalytic.Course.CONTINUE_PRESSED, mapOf(
                    AmplitudeAnalytic.Course.Params.COURSE to course.id,
                    AmplitudeAnalytic.Course.Params.SOURCE to AmplitudeAnalytic.Course.Values.COURSE_WIDGET
            ))
            continueCoursePresenter.continueCourse(course) //provide position?
        } else {
            screenManager.showCourseDescription(contextActivity, course, true)
        }
    }

    fun setDataOnView(course: Course) {
        courseItemName.text = course.title
        Glide
                .with(itemView.context)
                .load(StepikLogicHelper.getPathForCourseOrEmpty(course, config))
                .asBitmap()
                .placeholder(coursePlaceholder)
                .fitCenter()
                .into(imageViewTarget)

        val needShowLearners = course.learnersCount > 0
        if (needShowLearners) {
            learnersCountText.text = String.format(Locale.getDefault(), "%d", course.learnersCount)
        }
        learnersCountImage.changeVisibility(needShowLearners)
        learnersCountText.changeVisibility(needShowLearners)

        if (isEnrolled(course)) {
            courseWidgetInfo.setText(R.string.course_item_syllabus)
            showContinueButton()
        } else {
            courseWidgetInfo.setText(R.string.course_item_info)
            showJoinButton()
        }

        val needShowProgress = bindProgressView(course)
        val needShowRating = bindRatingView(course)

        val showContainer = needShowLearners || needShowProgress || needShowRating
        coursePropertiesContainer.changeVisibility(showContainer)

        courseItemMore.changeVisibility(showMore)

        adaptiveCourseMarker.changeVisibility(adaptiveCoursesResolver.isAdaptive(course.id))

        this.course = course
    }

    private fun bindProgressView(course: Course): Boolean {
        val progressPercent: Int? = ProgressUtil.getProgressPercent(course.progressObject)
        val needShow =
                if (progressPercent != null && progressPercent > 0) {
                    prepareViewForProgress(progressPercent)
                    true
                } else {
                    false
                }
        courseItemProgress.changeVisibility(needShow)
        courseItemProgressTitle.changeVisibility(needShow)
        return needShow
    }

    private fun prepareViewForProgress(progressPercent: Int) {
        courseItemProgress.progress = progressPercent / 100f
        courseItemProgressTitle.text = itemView
                .resources
                .getString(R.string.percent_symbol, progressPercent)
    }

    private fun bindRatingView(course: Course): Boolean {
        val needShow = course.rating > 0
        if (needShow) {
            courseRatingText.text = String.format(Locale.ROOT, itemView.resources.getString(R.string.course_rating_value), course.rating)
        }
        courseRatingImage.changeVisibility(needShow)
        courseRatingText.changeVisibility(needShow)
        return needShow
    }

    private fun isEnrolled(course: Course?): Boolean =
            course != null && course.enrollment != 0L

    private fun showJoinButton() {
        courseWidgetButton.applyToButton(joinTitle, joinColor, colorType.joinResource)
    }

    private fun showContinueButton() {
        courseWidgetButton.applyToButton(continueTitle, continueColor, colorType.continueResource)
    }

    private fun TextView.applyToButton(title: String, @ColorInt textColor: Int, @DrawableRes background: Int) {
        this.text = title
        this.setTextColor(textColor)
        this.setBackgroundResource(background)
    }

}