package org.stepik.android.view.course_content.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.model.Course
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import javax.inject.Inject

class RemoveCachedContentDialog : DialogFragment() {
    companion object {
        const val TAG = "RemoveCachedContentDialog"

        private const val ARG_COURSE = "course"
        private const val ARG_SECTION = "section"
        private const val ARG_UNIT = "unit"

        fun newInstance(
            course: Course? = null,
            section: Section? = null,
            unit: Unit? = null
        ): DialogFragment =
            RemoveCachedContentDialog()
                .apply {
                    arguments = Bundle(3)
                        .also {
                            it.putParcelable(ARG_COURSE, course)
                            it.putParcelable(ARG_SECTION, section)
                            it.putParcelable(ARG_UNIT, unit)
                        }
                }
    }

    init {
        App.component().inject(this)
    }

    @Inject
    lateinit var analytic: Analytic

    private val course: Course? by lazy { arguments?.getParcelable<Course>(ARG_COURSE) }
    private val section: Section? by lazy { arguments?.getParcelable<Section>(ARG_SECTION) }
    private val unit: Unit? by lazy { arguments?.getParcelable<Unit>(ARG_UNIT) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.course_content_remove_item_title)
            .setMessage(R.string.course_content_remove_item_description)
            .setPositiveButton(R.string.delete_label) { _, _ ->
                val callback = targetFragment as? Callback
                    ?: parentFragment as? Callback
                    ?: activity as? Callback
                    ?: return@setPositiveButton

                analytic.reportAmplitudeEvent(
                    AmplitudeAnalytic.Downloads.DELETE_CONFIRMATION_INTERACTED,
                    mapOf(
                        AmplitudeAnalytic.Downloads.PARAM_CONTENT to getAmplitudeContentParameterValue(),
                        AmplitudeAnalytic.Downloads.PARAM_RESULT to AmplitudeAnalytic.Downloads.Values.YES
                    )
                )

                course?.let(callback::onRemoveCourseDownloadConfirmed)
                section?.let(callback::onRemoveSectionDownloadConfirmed)
                unit?.let(callback::onRemoveUnitDownloadConfirmed)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                handleCancelAction()
            }
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(context.resolveColorAttribute(R.attr.colorError))
                }
            }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        handleCancelAction()
    }

    private fun handleCancelAction() {
        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.Downloads.DELETE_CONFIRMATION_INTERACTED,
            mapOf(
                AmplitudeAnalytic.Downloads.PARAM_CONTENT to getAmplitudeContentParameterValue(),
                AmplitudeAnalytic.Downloads.PARAM_RESULT to AmplitudeAnalytic.Downloads.Values.NO
            )
        )
    }

    private fun getAmplitudeContentParameterValue(): String =
        when {
            course != null ->
                AmplitudeAnalytic.Downloads.Values.COURSE
            section != null ->
                AmplitudeAnalytic.Downloads.Values.SECTION
            unit != null ->
                AmplitudeAnalytic.Downloads.Values.LESSON
            else ->
                ""
        }

    interface Callback {
        fun onRemoveCourseDownloadConfirmed(course: Course) {}
        fun onRemoveSectionDownloadConfirmed(section: Section) {}
        fun onRemoveUnitDownloadConfirmed(unit: Unit) {}
    }
}