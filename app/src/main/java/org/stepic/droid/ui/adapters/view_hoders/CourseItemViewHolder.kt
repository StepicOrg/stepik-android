package org.stepic.droid.ui.adapters.view_hoders

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.PopupMenu
import android.view.HapticFeedbackConstants
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import kotlinx.android.synthetic.main.new_course_item.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.configuration.Config
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.presenters.ContinueCoursePresenter
import org.stepic.droid.core.presenters.DroppingPresenter
import org.stepic.droid.model.Course
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.ContextMenuCourseUtil
import org.stepic.droid.util.StepikLogicHelper
import java.util.*
import javax.inject.Inject

class CourseItemViewHolder(
        val view: View,
        private val contextActivity: Activity,
        private val type: Table?,
        private val joinTitle: String,
        private val continueTitle: String,
        private val coursePlaceholder: Drawable,
        private val isContinueExperimentEnabled: Boolean,
        private val courses: List<Course>,
        private val droppingPresenter: DroppingPresenter,
        private val continueCoursePresenter: ContinueCoursePresenter) : CourseViewHolderBase(view) {

    @Inject
    lateinit var screenManager: ScreenManager

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var config: Config


    private val continueColor: Int by lazy {
        ColorUtil.getColorArgb(R.color.new_accent_color, contextActivity)
    }
    private val joinColor: Int by lazy {
        ColorUtil.getColorArgb(R.color.join_text_color, contextActivity)
    }
    private var imageViewTarget: BitmapImageViewTarget

    init {
        App.component().inject(this)

        imageViewTarget = object : BitmapImageViewTarget(itemView.courseItemImage) {
            override fun setResource(resource: Bitmap) {
                val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(contextActivity.resources, resource)
                circularBitmapDrawable.cornerRadius = contextActivity.resources.getDimension(R.dimen.course_image_radius)
                itemView.courseItemImage.setImageDrawable(circularBitmapDrawable)
            }
        }
        itemView.courseWidgetButton.setOnClickListener {
            val adapterPosition = adapterPosition
            val course = getCourseSafety(adapterPosition)
            if (course != null) {
                onClickWidgetButton(course, isEnrolled(course))
            }
        }
        itemView.setOnClickListener({ onClickCourse(adapterPosition) })

        itemView.setOnLongClickListener({ v ->
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            itemView.showContextMenu()
            true
        })

        itemView.courseItemMore.setOnClickListener { v ->
            val course = getCourseSafety(adapterPosition)
            if (course != null) {
                showMore(v, course)
            }
        }
    }


    private fun showMore(view: View, course: Course) {
        val morePopupMenu = PopupMenu(contextActivity, view)
        morePopupMenu.inflate(ContextMenuCourseUtil.getMenuResource(course))

        morePopupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_info -> {
                    screenManager.showCourseDescription(contextActivity, course)
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


    private fun onClickCourse(position: Int) {
        if (position >= courses.size || position < 0) return
        analytic.reportEvent(Analytic.Interaction.CLICK_COURSE)
        val course = courses[position]
        if (course.enrollment != 0) {
            analytic.reportEvent(if (isContinueExperimentEnabled) Analytic.ContinueExperiment.COURSE_NEW else Analytic.ContinueExperiment.COURSE_OLD)
            screenManager.showSections(contextActivity, course)
        } else {
            screenManager.showCourseDescription(contextActivity, course)
        }
    }


    private fun onClickWidgetButton(course: Course, enrolled: Boolean) {
        if (enrolled) {
            analytic.reportEvent(Analytic.Interaction.CLICK_CONTINUE_COURSE)
            analytic.reportEvent(if (isContinueExperimentEnabled) Analytic.ContinueExperiment.CONTINUE_NEW else Analytic.ContinueExperiment.CONTINUE_OLD)
            continueCoursePresenter.continueCourse(course) //provide position?
        } else {
            screenManager.showCourseDescription(contextActivity, course)
        }
    }

    private fun getCourseSafety(adapterPosition: Int): Course? {
        return if (adapterPosition >= courses.size || adapterPosition < 0) {
            null
        } else {
            courses[adapterPosition]
        }
    }

    override fun setDataOnView(position: Int) {
        val course = courses[position]

        itemView.courseItemName.text = course.title
        Glide
                .with(contextActivity)
                .load(StepikLogicHelper.getPathForCourseOrEmpty(course, config))
                .asBitmap()
                .placeholder(coursePlaceholder)
                .fitCenter()
                .into(imageViewTarget)

        if (course.learnersCount > 0) {
            itemView.courseItemLearnersCount.text = String.format(Locale.getDefault(), "%d", course.learnersCount)
            itemView.courseItemLearnersCount.visibility = View.VISIBLE
        } else {
            itemView.courseItemLearnersCount.visibility = View.GONE
        }


        if (isEnrolled(course)) {
            showContinueButton()
        } else {
            showJoinButton()
        }

        itemView.courseItemMore.visibility = if (type === Table.enrolled) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun isEnrolled(course: Course?): Boolean =
            course != null && course.enrollment != 0 && course.isActive && course.lastStepId != null

    private fun showJoinButton() {
        showButton(joinTitle, joinColor, R.drawable.course_widget_join_background)
    }

    private fun showContinueButton() {
        showButton(continueTitle, continueColor, R.drawable.course_widget_continue_background)
    }

    private fun showButton(title: String, @ColorInt textColor: Int, @DrawableRes background: Int) {
        itemView.courseWidgetButton.text = title
        itemView.courseWidgetButton.setTextColor(textColor)
        itemView.courseWidgetButton.setBackgroundResource(background)
    }

}