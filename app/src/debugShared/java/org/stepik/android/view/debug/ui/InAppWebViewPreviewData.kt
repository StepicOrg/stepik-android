package org.stepik.android.view.debug.ui

import androidx.fragment.app.Fragment
import org.stepik.android.view.in_app_web_view.ui.dialog.InAppWebViewDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists

object InAppWebViewPreviewData {
    fun show(host: Fragment) {
        InAppWebViewDialogFragment
            .newInstance(title = "Sample web page", url = "https://example.com")
            .showIfNotExists(host.childFragmentManager, InAppWebViewDialogFragment.TAG)
    }
}
