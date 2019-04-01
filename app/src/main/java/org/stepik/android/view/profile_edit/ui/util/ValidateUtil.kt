package org.stepik.android.view.profile_edit.ui.util

import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.TextUtils
import org.stepic.droid.R

object ValidateUtil {
    fun validateRequiredField(layout: TextInputLayout, editText: TextInputEditText): Boolean {
        val value = (editText.text ?: "").trim()
        val valid = !TextUtils.isEmpty(value)
        if (valid) {
            layout.isErrorEnabled = false
        } else {
            layout.error = layout.context.getString(R.string.profile_edit_error_required_field)
        }
        return valid
    }
}