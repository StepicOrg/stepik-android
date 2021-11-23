package org.stepik.android.view.certificate.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.core.text.buildSpannedString
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepik.android.model.Certificate
import ru.nobird.android.view.base.ui.extension.argument

class CertificateNameChangeConfirmationDialog : DialogFragment() {
    companion object {
        const val TAG = "CertificateChangeNameConfirmationDialog"

        fun newInstance(newFullName: String, certificate: Certificate): DialogFragment =
            CertificateNameChangeConfirmationDialog().apply {
                this.newFullName = newFullName
                this.certificate = certificate
            }
    }

    private var newFullName: String by argument()
    private var certificate: Certificate by argument()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = buildSpannedString {
            append(resources.getString(R.string.certificate_name_change_dialog_body_confirmation, certificate.savedFullName, newFullName))
            if (certificate.editsCount + 1 == certificate.allowedEditsCount) {
                append("\n")
                append(resources.getString(R.string.certificate_name_change_dialog_body_last_edit_warning))
            }
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.certificate_name_change_recipient_name)
            .setMessage(message)
            .setCancelable(false)
            .setNegativeButton(R.string.cancel) { _, _ ->
                dismiss()
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                (activity as? Callback)?.updateCertificate(certificate.copy(savedFullName = newFullName))
            }
            .create()
    }

    interface Callback {
        fun updateCertificate(certificate: Certificate)
    }
}