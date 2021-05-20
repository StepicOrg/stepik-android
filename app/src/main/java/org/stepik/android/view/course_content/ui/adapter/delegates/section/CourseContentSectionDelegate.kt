package org.stepik.android.view.course_content.ui.adapter.delegates.section

import android.graphics.PorterDuff
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.collection.LongSparseArray
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_course_content_section.view.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.ui.util.StartSnapHelper
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.toFixed
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.ui.adapter.CourseContentTimelineAdapter
import org.stepik.android.view.course_content.ui.adapter.decorators.CourseContentTimelineDecorator
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.view.base.ui.extension.getDrawableCompat
import kotlin.math.roundToInt

class CourseContentSectionDelegate(
    private val sectionClickListener: CourseContentSectionClickListener,
    private val sectionDownloadStatuses: LongSparseArray<DownloadProgress.Status>
) : AdapterDelegate<CourseContentItem, DelegateViewHolder<CourseContentItem>>() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder =
        ViewHolder(createView(parent, R.layout.view_course_content_section))

    override fun isForViewType(position: Int, data: CourseContentItem): Boolean =
        data is CourseContentItem.SectionItem

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseContentItem>(root) {
        private val sectionExamType = root.sectionExamType
        private val sectionExamStatus = root.sectionExamStatus
        private val sectionTitle    = root.sectionTitle
        private val sectionPosition = root.sectionPosition
        private val sectionTimeline = root.sectionTimeline
        private val sectionProgress = root.sectionProgress
        private val sectionTextProgress    = root.sectionTextProgress
        private val sectionDownloadStatus  = root.sectionDownloadStatus
        private val sectionRequirementsDescription = root.sectionRequirementsDescription
        private val sectionExamAction = root.sectionExamAction

        private val sectionTimeLineAdapter =
            CourseContentTimelineAdapter()

        init {
            with(sectionTimeline) {
                adapter = sectionTimeLineAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                StartSnapHelper().attachToRecyclerView(this)
                addItemDecoration(CourseContentTimelineDecorator())

                this@ViewHolder.sectionTitle.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        setPadding(this@ViewHolder.sectionTitle.left, paddingTop, paddingRight, paddingBottom)
                        layoutManager?.scrollToPosition(0)

                        this@ViewHolder.sectionTitle.viewTreeObserver.removeOnPreDrawListener(this)
                        return true
                    }
                })
            }

            sectionDownloadStatus.setOnClickListener {
                val item = (itemData as? CourseContentItem.SectionItem) ?: return@setOnClickListener
                when (sectionDownloadStatus.status) {
                    DownloadProgress.Status.NotCached ->
                        sectionClickListener.onItemDownloadClicked(item)

                    is DownloadProgress.Status.InProgress ->
                        sectionClickListener.onItemCancelClicked(item)

                    is DownloadProgress.Status.Cached ->
                        sectionClickListener.onItemRemoveClicked(item)
                }
            }

            sectionExamAction.setOnClickListener {
                val item = (itemData as? CourseContentItem.SectionItem) ?: return@setOnClickListener
                sectionClickListener.onItemClicked(item)
            }
        }

        override fun onBind(data: CourseContentItem) {
            with(data as CourseContentItem.SectionItem) {

                setupExamViews(data)

                sectionExamType.isVisible = section.isExam
                sectionExamStatus.isVisible = section.isExam

                sectionTitle.text = section.title
                sectionPosition.text = section.position.toString()

                if (progress != null) {
                    when {
                        progress.cost > 0 -> {
                            val score = progress
                                .score
                                ?.toFloatOrNull()
                                ?: 0f

                            sectionProgress.progress = score / progress.cost.toFloat()
                            sectionTextProgress.text = context.resources.getString(R.string.course_content_text_progress_points,
                                score.toFixed(context.resources.getInteger(R.integer.score_decimal_count)), progress.cost)
                            sectionTextProgress.visibility = View.VISIBLE
                        }
                        progress.cost == 0L && data.section.isExam && getExamStatus(data) == ExamStatus.FINISHED -> {
                            sectionTextProgress.text = context.resources.getString(R.string.section_syllabus_exam_no_score_title)
                            sectionProgress.progress = 0f
                            sectionTextProgress.visibility = View.VISIBLE
                        }
                        else -> {
                            sectionProgress.progress = 0f
                            sectionTextProgress.visibility = View.GONE
                        }
                    }
                } else {
                    sectionProgress.progress = 0f
                    sectionTextProgress.visibility = View.GONE
                }

                sectionDownloadStatus.status = sectionDownloadStatuses[data.section.id] ?: DownloadProgress.Status.Pending
                sectionTimeLineAdapter.dates = dates
                sectionTimeline.isVisible = dates.isNotEmpty()

                sectionDownloadStatus.isVisible = isEnabled && !section.isExam

                val alpha = if (isEnabled) 1f else 0.4f
                sectionTitle.alpha = alpha
                sectionPosition.alpha = alpha
                sectionTimeline.alpha = alpha

                if (requiredSection != null) {
                    val requiredPoints = (requiredSection.progress.cost * section.requiredPercent / 100f).roundToInt()

                    sectionRequirementsDescription.text =
                        context.getString(
                            R.string.course_content_section_requirements,
                            context.resources.getQuantityString(R.plurals.points, requiredPoints.toInt(), requiredPoints),
                            requiredSection.section.title
                        )

                    sectionRequirementsDescription.isVisible = true
                } else {
                    sectionRequirementsDescription.isVisible = false
                }
            }
        }

        private fun getExamStatus(sectionItem: CourseContentItem.SectionItem): ExamStatus =
            when {
                isExamCanStart(sectionItem) -> ExamStatus.IS_CAN_START
                isExamActive(sectionItem) -> ExamStatus.IN_PROGRESS
                isExamFinished(sectionItem) -> ExamStatus.FINISHED
                else -> ExamStatus.CANNOT_START
            }

        private fun isExamCanStart(sectionItem: CourseContentItem.SectionItem): Boolean {
            if (!sectionItem.section.isExam) {
                return false
            }

            val isReachable = (sectionItem.section.isActive || sectionItem.section.actions?.testSection != null) && (sectionItem.section.progress != null || sectionItem.section.isExam)
            if (!isReachable) {
                return false
            }

            if (sectionItem.examSession != null) {
                return false
            }

            if (sectionItem.proctorSession?.isFinished == true) {
                return false
            }

            if (sectionItem.section.actions?.testSection != null) {
                return false
            }

            val isExamTime = (sectionItem.section.beginDate == null || (sectionItem.section.beginDate?.time!! < DateTimeHelper.nowUtc()) && (sectionItem.section.endDate == null || (DateTimeHelper.nowUtc() < sectionItem.section.endDate?.time!!)))
            val isRequirementSatisfied = sectionItem.section.isRequirementSatisfied
            return isExamTime && isRequirementSatisfied
        }

        private fun isExamActive(sectionItem: CourseContentItem.SectionItem): Boolean =
            (sectionItem.examSession?.isActive ?: false) && !(sectionItem.proctorSession?.isFinished ?: false)

        private fun isExamFinished(sectionItem: CourseContentItem.SectionItem): Boolean {
            if (isExamCanStart(sectionItem) || isExamActive(sectionItem)) {
                return false
            }

            val flag = sectionItem.section.endDate?.let { it.time < DateTimeHelper.nowUtc() } ?: false

            return flag || (sectionItem.proctorSession?.isFinished ?: false) || sectionItem.examSession?.id != null
        }

        private fun setupExamViews(sectionItem: CourseContentItem.SectionItem) {
            sectionExamType.text =  if (sectionItem.isProctored) {
                context.getString(R.string.section_syllabus_exam_chip_proctored_title)
            } else {
                context.getString(R.string.section_syllabus_exam_chip_simple_title)
            }
            sectionExamType.background = getColoredDrawable(
                R.drawable.bg_shape_rounded_16dp,
                ContextCompat.getColor(context, R.color.color_overlay_violet_alpha_12)
            )

            val examStatus = getExamStatus(sectionItem)

            when (examStatus) {
                ExamStatus.IS_CAN_START, ExamStatus.CANNOT_START -> {
                    val clockDrawable = getColoredDrawable(
                        R.drawable.ic_clock,
                        ContextCompat.getColor(context, R.color.color_overlay_green)
                    )
                    sectionExamStatus.setCompoundDrawablesWithIntrinsicBounds(clockDrawable, null, null, null)
                    sectionExamStatus.setTextColor(ContextCompat.getColor(context, R.color.color_overlay_green))
                    sectionExamStatus.text = sectionItem.section.examDurationMinutes?.let {
                        context.resources.getQuantityString(R.plurals.minutes,
                            it, sectionItem.section.examDurationMinutes)
                    }
                    sectionExamStatus.background = getColoredDrawable(
                        R.drawable.bg_shape_rounded_16dp,
                        ContextCompat.getColor(context, R.color.color_overlay_green_alpha_12)
                    )
                }
                ExamStatus.IN_PROGRESS -> {
                    val evaluationDrawable = AnimationDrawable()
                    evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_1), 250)
                    evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_2), 250)
                    evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_3), 250)
                    evaluationDrawable.isOneShot = false
                    DrawableCompat.setTint(evaluationDrawable, ContextCompat.getColor(context, R.color.white))
                    sectionExamStatus.setCompoundDrawablesWithIntrinsicBounds(evaluationDrawable, null, null, null)
                    evaluationDrawable.start()

                    sectionExamStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                    sectionExamStatus.text = context.getString(R.string.section_syllabus_exam_in_progress)
                    sectionExamStatus.background = getColoredDrawable(
                        R.drawable.bg_shape_rounded_16dp,
                        ContextCompat.getColor(context, R.color.color_overlay_violet)
                    )
                }
                ExamStatus.FINISHED -> {
                    val checkDrawable = getColoredDrawable(
                        R.drawable.ic_exam_finished,
                        ContextCompat.getColor(context, R.color.white)
                    )
                    sectionExamStatus.setCompoundDrawablesWithIntrinsicBounds(checkDrawable, null, null, null)
                    sectionExamStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                    sectionExamStatus.text = context.getString(R.string.section_syllabus_exam_finished)
                    sectionExamStatus.background = getColoredDrawable(
                        R.drawable.bg_shape_rounded_16dp,
                        ContextCompat.getColor(context, R.color.color_overlay_green)
                    )
                }
            }

            val examActionTitle = when (examStatus) {
                ExamStatus.IS_CAN_START ->
                    context.getString(R.string.section_syllabus_exam_action_start)
                ExamStatus.IN_PROGRESS ->
                    context.getString(R.string.section_syllabus_exam_action_continue)
                ExamStatus.FINISHED ->
                    context.getString(R.string.section_syllabus_exam_action_finished)
                ExamStatus.CANNOT_START ->
                    ""
            }
            sectionExamAction.text = examActionTitle
            sectionExamAction.isVisible = examActionTitle.isNotEmpty() && sectionItem.isEnabled
        }

        private fun getColoredDrawable(@DrawableRes resId: Int, @ColorInt color: Int): Drawable? =
            AppCompatResources
                .getDrawable(context, resId)
                ?.mutate()
                ?.let { DrawableCompat.wrap(it) }
                ?.also {
                    DrawableCompat.setTint(it, color)
                    DrawableCompat.setTintMode(it, PorterDuff.Mode.SRC_IN)
                }
    }

    enum class ExamStatus {
        IS_CAN_START,
        CANNOT_START,
        IN_PROGRESS,
        FINISHED
    }
}