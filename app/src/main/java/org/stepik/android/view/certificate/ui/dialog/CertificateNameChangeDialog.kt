package org.stepik.android.view.certificate.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_certificate_name_change.view.*
import org.stepic.droid.R
import org.stepik.android.model.Certificate
import ru.nobird.android.view.base.ui.extension.argument

class CertificateNameChangeDialog : DialogFragment() {
    companion object {
        const val TAG = "CertificateChangeNameDialog"

        fun newInstance(certificate: Certificate, attemptedFullName: String): DialogFragment =
            CertificateNameChangeDialog().apply {
                this.certificate = certificate
                this.attemptedFullName = attemptedFullName
            }
    }

    private var certificate: Certificate by argument()
    private var attemptedFullName: String by argument()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(requireContext(), R.layout.dialog_certificate_name_change, null)
        val editTextWrapper = view.certificateChangeNameWrapper
        val editText = view.certificateChangeNameEditText

        editText.setText(attemptedFullName)
        editText.doAfterTextChanged { editTextWrapper.error = null }
        editText.postDelayed({
            editText.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, 200)

        if (attemptedFullName.isNotEmpty()) {
            editTextWrapper.error = resources.getString(R.string.certificate_name_change_failure)
        }

        val remainingEdits = certificate.allowedEditsCount - certificate.editsCount
        view.certificateChangeNameBody.text = resources.getString(
            R.string.certificate_name_change_dialog_body_warning,
            resources.getQuantityString(
                R.plurals.times,
                remainingEdits,
                remainingEdits
            )
        )

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.certificate_name_change_dialog_title)
            .setView(view)
            .setCancelable(false)
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setPositiveButton(R.string.ok, null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (editText.text.isNullOrBlank()) {
                            editTextWrapper.error =
                                resources.getString(R.string.certificate_name_change_empty_field_error)
                        } else {
                            (activity as? Callback)?.showUpdateCertificateConfirmationDialog(editText.text.toString(), certificate)
                            dismiss()
                        }
                    }
                }
            }
    }

    interface Callback {
        fun showUpdateCertificateConfirmationDialog(newFullName: String, certificate: Certificate)
    }
}