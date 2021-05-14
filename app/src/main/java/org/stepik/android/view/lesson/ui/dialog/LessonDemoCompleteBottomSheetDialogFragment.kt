package org.stepik.android.view.lesson.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.stepic.droid.R
import org.stepik.android.model.Course

class LessonDemoCompleteBottomSheetDialogFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "LessonDemoCompleteBottomSheetDialog"

        const val ARG_COURSE = "course"

        fun newInstance(course: Course): DialogFragment =
            LessonDemoCompleteBottomSheetDialogFragment().apply {
                    this.arguments = Bundle(1)
                        .also {
                            it.putParcelable(ARG_COURSE, course)
                        }
                }
    }

    private val course: Course? by lazy { arguments?.getParcelable<Course>(ARG_COURSE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomBottomSheetDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_lesson_demo_complete, container, false)

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }
}