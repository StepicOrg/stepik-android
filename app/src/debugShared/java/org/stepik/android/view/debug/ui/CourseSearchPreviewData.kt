package org.stepik.android.view.debug.ui

import androidx.fragment.app.Fragment
import org.stepik.android.view.course_search.dialog.CourseSearchDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists

object CourseSearchPreviewData {
    fun show(host: Fragment) {
        CourseSearchDialogFragment
            .newInstance(courseId = -1L, courseTitle = "Sample course")
            .showIfNotExists(host.childFragmentManager, CourseSearchDialogFragment.TAG)
    }
}
