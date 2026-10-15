package org.stepik.android.view.debug.ui

import androidx.fragment.app.Fragment
import org.stepik.android.model.Step
import org.stepik.android.view.submission.ui.dialog.SubmissionsDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists

object SubmissionsPreviewData {
    fun show(host: Fragment) {
        SubmissionsDialogFragment
            .newInstance(step = Step(id = -1L))
            .showIfNotExists(host.childFragmentManager, SubmissionsDialogFragment.TAG)
    }
}
