package org.stepik.android.view.lesson.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_dialog_lesson_demo_complete.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.model.Course
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class LessonDemoCompleteBottomSheetDialogFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "LessonDemoCompleteBottomSheetDialog"

        const val ARG_COURSE = "course"

        fun newInstance(course: Course): DialogFragment =
            LessonDemoCompleteBottomSheetDialogFragment().apply {
                this.course = course
            }
    }

    private var course: Course by argument()

    @Inject
    lateinit var screenManager: ScreenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TopCornersRoundedBottomSheetDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_lesson_demo_complete, container, false)

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        demoCompleteTitle.text = getString(R.string.demo_complete_title, course.title)
        demoCompleteAction.text = getString(R.string.demo_complete_purchase_action, course.displayPrice)
        demoCompleteAction.setOnClickListener {
            screenManager.showCoursePurchase(requireContext(), course.id, CourseViewSource.LessonDemoDialog)
        }
    }
}