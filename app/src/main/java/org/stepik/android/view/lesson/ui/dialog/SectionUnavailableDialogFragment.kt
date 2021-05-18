package org.stepik.android.view.lesson.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.view.step.model.SectionUnavailableAction
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.resolveAttribute
import java.util.TimeZone

class SectionUnavailableDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "SectionUnavailableDialogFragment"

        fun newInstance(sectionUnavailableAction: SectionUnavailableAction): DialogFragment =
            SectionUnavailableDialogFragment()
                .apply {
                    this.sectionUnavailableAction = sectionUnavailableAction
                }
    }

    private var sectionUnavailableAction: SectionUnavailableAction by argument()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
        return when (val action = sectionUnavailableAction) {
            is SectionUnavailableAction.RequiresSection ->
                setupRequiresSectionDialog(action, alertDialog)
            is SectionUnavailableAction.RequiresExam ->
                setupRequiresExamDialog(action, alertDialog)
            is SectionUnavailableAction.RequiresDate ->
                setupRequiresDateDialog(action, alertDialog)
        }
    }

    private fun setupRequiresSectionDialog(requiresSectionAction: SectionUnavailableAction.RequiresSection, materialAlertDialogBuilder: MaterialAlertDialogBuilder): Dialog {
        val title = getString(R.string.unavailable_section_title, requiresSectionAction.currentSection.title)
        val requiredPoints = requiresSectionAction.requiredProgress.cost * requiresSectionAction.requiredSection.requiredPercent / 100
        val message = getString(
            R.string.unavailable_section_required_section_message,
            requiresSectionAction.targetSection.title,
            resources.getQuantityString(R.plurals.points, requiredPoints.toInt(), requiredPoints),
            requiresSectionAction.requiredSection.title
        )

        return with(materialAlertDialogBuilder) {
            setTitle(title)
            setMessage(message)
            setNegativeButton(R.string.unavailable_section_to_content_action) { _, _ ->
                (parentFragment as? Callback)?.onSyllabusAction(CourseViewSource.SectionUnavailableDialog)
            }
            setPositiveButton(R.string.unavailable_section_to_tasks_action) { _, _ ->
                dismiss()
            }
            create()
        }.apply {
            setOnShowListener {
                val negativeColor = context
                    .resolveAttribute(R.attr.colorControlNormal)
                    ?.data
                    ?: return@setOnShowListener

                val positiveColor = ContextCompat
                    .getColor(context, R.color.color_overlay_green)

                getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(negativeColor)
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(positiveColor)
            }
        }
    }

    private fun setupRequiresExamDialog(requiresExamAction: SectionUnavailableAction.RequiresExam, materialAlertDialogBuilder: MaterialAlertDialogBuilder): Dialog {
        val title = getString(R.string.unavailable_section_title, requiresExamAction.currentSection.title)
        val message = getString(R.string.unavailable_section_required_exam_message, requiresExamAction.targetSection.title)
        return with(materialAlertDialogBuilder) {
            setTitle(title)
            setMessage(message)
            setPositiveButton(R.string.unavailable_section_to_content_action) { _, _ ->
                (parentFragment as? Callback)?.onSyllabusAction(CourseViewSource.SectionUnavailableDialog)
            }
            create()
        }.apply {
            setOnShowListener {
                val positiveColor = ContextCompat
                    .getColor(context, R.color.color_overlay_green)

                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(positiveColor)
            }
        }
    }

    private fun setupRequiresDateDialog(requiresDateAction: SectionUnavailableAction.RequiresDate, materialAlertDialogBuilder: MaterialAlertDialogBuilder): Dialog {
        val title = getString(R.string.unavailable_section_title, requiresDateAction.currentSection.title)
        val message = getString(
            R.string.unavailable_section_required_date_message,
            requiresDateAction.nextLesson.title,
            DateTimeHelper.getPrintableDate(requiresDateAction.date, DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault())
        )
        return with(materialAlertDialogBuilder) {
            setTitle(title)
            setMessage(message)
            setPositiveButton(R.string.unavailable_section_to_content_action) { _, _ ->
                (parentFragment as? Callback)?.onSyllabusAction(CourseViewSource.SectionUnavailableDialog)
            }
            create()
        }.apply {
            setOnShowListener {
                val positiveColor = ContextCompat
                    .getColor(context, R.color.color_overlay_green)

                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(positiveColor)
            }
        }
    }

    interface Callback {
        fun onSyllabusAction(courseViewSource: CourseViewSource)
    }
}