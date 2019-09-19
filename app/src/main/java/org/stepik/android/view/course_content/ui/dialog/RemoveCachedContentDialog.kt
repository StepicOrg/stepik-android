package org.stepik.android.view.course_content.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepic.droid.R
import org.stepik.android.model.Course
import org.stepik.android.model.Section
import org.stepik.android.model.Unit

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

    private val course: Course? by lazy { arguments?.getParcelable<Course>(ARG_COURSE) }
    private val section: Section? by lazy { arguments?.getParcelable<Section>(ARG_SECTION) }
    private val unit: Unit? by lazy { arguments?.getParcelable<Unit>(ARG_UNIT) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog
            .Builder(requireContext())
            .setMessage(R.string.deleted_key)
            .setPositiveButton(R.string.delete_label) { _, _ ->
                val callback = targetFragment as? Callback
                    ?: parentFragment as? Callback
                    ?: activity as? Callback
                    ?: return@setPositiveButton

                course?.let(callback::onRemoveCourseDownloadConfirmed)
                section?.let(callback::onRemoveSectionDownloadConfirmed)
                unit?.let(callback::onRemoveUnitDownloadConfirmed)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                dismiss()
            }
            .create()

    interface Callback {
        fun onRemoveCourseDownloadConfirmed(course: Course)
        fun onRemoveSectionDownloadConfirmed(section: Section)
        fun onRemoveUnitDownloadConfirmed(unit: Unit)
    }
}