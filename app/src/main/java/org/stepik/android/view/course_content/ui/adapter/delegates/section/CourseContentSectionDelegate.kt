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
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewCourseContentSectionBinding
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.ui.util.StartSnapHelper
import org.stepic.droid.util.toFixed
import org.stepik.android.domain.exam.model.ExamStatus
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
        private val viewBinding: ViewCourseContentSectionBinding by viewBinding { ViewCourseContentSectionBinding.bind(root) }

        private val sectionTimeLineAdapter =
            CourseContentTimelineAdapter()

        init {
            with(viewBinding.sectionTimeline) {
                adapter = sectionTimeLineAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                StartSnapHelper().attachToRecyclerView(this)
                addItemDecoration(CourseContentTimelineDecorator())

                this@ViewHolder.viewBinding.sectionTitle.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        setPadding(this@ViewHolder.viewBinding.sectionTitle.left, paddingTop, paddingRight, paddingBottom)
                        layoutManager?.scrollToPosition(0)

                        this@ViewHolder.viewBinding.sectionTitle.viewTreeObserver.removeOnPreDrawListener(this)
                        return true
                    }
                })
            }

            viewBinding.sectionDownloadStatus.setOnClickListener {
                val item = (itemData as? CourseContentItem.SectionItem) ?: return@setOnClickListener
                when (viewBinding.sectionDownloadStatus.status) {
                    DownloadProgress.Status.NotCached ->
                        sectionClickListener.onItemDownloadClicked(item)

                    is DownloadProgress.Status.InProgress ->
                        sectionClickListener.onItemCancelClicked(item)

                    is DownloadProgress.Status.Cached ->
                        sectionClickListener.onItemRemoveClicked(item)

                    else -> Unit
                }
            }

            viewBinding.sectionExamAction.setOnClickListener {
                val item = (itemData as? CourseContentItem.SectionItem) ?: return@setOnClickListener
                sectionClickListener.onItemClicked(item)
            }
        }

        override fun onBind(data: CourseContentItem) {
            with(data as CourseContentItem.SectionItem) {

                setupExamViews(data)

                viewBinding.sectionExamType.isVisible = section.isExam
                viewBinding.sectionExamStatus.isVisible = section.isExam

                viewBinding.sectionTitle.text = section.title
                viewBinding.sectionPosition.text = section.position.toString()

                if (progress != null) {
                    when {
                        progress.cost > 0 -> {
                            val score = progress
                                .score
                                ?.toFloatOrNull()
                                ?: 0f

                            viewBinding.sectionProgress.progress = score / progress.cost.toFloat()
                            viewBinding.sectionTextProgress.text = context.resources.getString(R.string.course_content_text_progress_points,
                                score.toFixed(context.resources.getInteger(R.integer.score_decimal_count)), progress.cost)
                            viewBinding.sectionTextProgress.visibility = View.VISIBLE
                        }
                        progress.cost == 0L && data.section.isExam && data.examStatus == ExamStatus.FINISHED -> {
                            viewBinding.sectionTextProgress.text = context.resources.getString(R.string.section_syllabus_exam_no_score_title)
                            viewBinding.sectionProgress.progress = 0f
                            viewBinding.sectionTextProgress.visibility = View.VISIBLE
                        }
                        else -> {
                            viewBinding.sectionProgress.progress = 0f
                            viewBinding.sectionTextProgress.visibility = View.GONE
                        }
                    }
                } else {
                    viewBinding.sectionProgress.progress = 0f
                    viewBinding.sectionTextProgress.visibility = View.GONE
                }

                viewBinding.sectionDownloadStatus.status = sectionDownloadStatuses[data.section.id] ?: DownloadProgress.Status.Pending
                sectionTimeLineAdapter.dates = dates
                viewBinding.sectionTimeline.isVisible = dates.isNotEmpty()

                viewBinding.sectionDownloadStatus.isVisible = isEnabled && !section.isExam

                val alpha = if (isEnabled) 1f else 0.4f
                viewBinding.sectionTitle.alpha = alpha
                viewBinding.sectionPosition.alpha = alpha
                viewBinding.sectionTimeline.alpha = alpha

                if (requiredSection != null) {
                    val requiredPoints = (requiredSection.progress.cost * section.requiredPercent / 100f).roundToInt()

                    viewBinding.sectionRequirementsDescription.text =
                        context.getString(
                            R.string.course_content_section_requirements,
                            context.resources.getQuantityString(R.plurals.points, requiredPoints.toInt(), requiredPoints),
                            requiredSection.section.title
                        )

                    viewBinding.sectionRequirementsDescription.isVisible = true
                } else {
                    viewBinding.sectionRequirementsDescription.isVisible = false
                }
            }
        }

        private fun setupExamViews(sectionItem: CourseContentItem.SectionItem) {
            viewBinding.sectionExamType.text =  if (sectionItem.isProctored) {
                context.getString(R.string.section_syllabus_exam_chip_proctored_title)
            } else {
                context.getString(R.string.section_syllabus_exam_chip_simple_title)
            }
            viewBinding.sectionExamType.background = getColoredDrawable(
                R.drawable.bg_shape_rounded_16dp,
                ContextCompat.getColor(context, R.color.color_overlay_violet_alpha_12)
            )

            when (sectionItem.examStatus) {
                ExamStatus.IS_CAN_START, ExamStatus.CANNOT_START -> {
                    val clockDrawable = getColoredDrawable(
                        R.drawable.ic_clock,
                        ContextCompat.getColor(context, R.color.color_overlay_green)
                    )
                    viewBinding.sectionExamStatus.setCompoundDrawablesWithIntrinsicBounds(clockDrawable, null, null, null)
                    viewBinding.sectionExamStatus.setTextColor(ContextCompat.getColor(context, R.color.color_overlay_green))
                    viewBinding.sectionExamStatus.text = sectionItem.section.examDurationMinutes?.let {
                        context.resources.getQuantityString(R.plurals.minutes,
                            it, sectionItem.section.examDurationMinutes)
                    }
                    viewBinding.sectionExamStatus.background = getColoredDrawable(
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
                    viewBinding.sectionExamStatus.setCompoundDrawablesWithIntrinsicBounds(evaluationDrawable, null, null, null)
                    evaluationDrawable.start()

                    viewBinding.sectionExamStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                    viewBinding.sectionExamStatus.text = context.getString(R.string.section_syllabus_exam_in_progress)
                    viewBinding.sectionExamStatus.background = getColoredDrawable(
                        R.drawable.bg_shape_rounded_16dp,
                        ContextCompat.getColor(context, R.color.color_overlay_violet)
                    )
                }
                ExamStatus.FINISHED -> {
                    val checkDrawable = getColoredDrawable(
                        R.drawable.ic_exam_finished,
                        ContextCompat.getColor(context, R.color.white)
                    )
                    viewBinding.sectionExamStatus.setCompoundDrawablesWithIntrinsicBounds(checkDrawable, null, null, null)
                    viewBinding.sectionExamStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                    viewBinding.sectionExamStatus.text = context.getString(R.string.section_syllabus_exam_finished)
                    viewBinding.sectionExamStatus.background = getColoredDrawable(
                        R.drawable.bg_shape_rounded_16dp,
                        ContextCompat.getColor(context, R.color.color_overlay_green)
                    )
                }

                else -> Unit
            }

            val examActionTitle = when (sectionItem.examStatus) {
                ExamStatus.IS_CAN_START ->
                    context.getString(R.string.section_syllabus_exam_action_start)
                ExamStatus.IN_PROGRESS ->
                    context.getString(R.string.section_syllabus_exam_action_continue)
                ExamStatus.FINISHED ->
                    context.getString(R.string.section_syllabus_exam_action_finished)
                ExamStatus.CANNOT_START ->
                    ""
                null ->
                    ""
            }
            viewBinding.sectionExamAction.text = examActionTitle
            viewBinding.sectionExamAction.isVisible = examActionTitle.isNotEmpty() && sectionItem.isEnabled
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
}
