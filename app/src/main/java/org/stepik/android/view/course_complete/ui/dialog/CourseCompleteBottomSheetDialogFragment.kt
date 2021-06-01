package org.stepik.android.view.course_complete.ui.dialog

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.model.Course
import ru.nobird.android.view.base.ui.extension.argument

class CourseCompleteBottomSheetDialogFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "CourseCompleteBottomSheetDialogFragment"

        const val ARG_COURSE = "course"

        fun newInstance(course: Course): DialogFragment =
            CourseCompleteBottomSheetDialogFragment().apply {
                this.course = course
            }
    }

    private var course: Course by argument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TopCornersRoundedBottomSheetDialog)
    }

    private fun injectComponent() {
        App.component()
            .courseCompleteComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }
}