package org.stepik.android.view.course_complete.model

import android.text.SpannedString
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class CourseCompleteDialogViewInfo(
    @DrawableRes
    val headerImage: Int,
    @DrawableRes
    val gradientRes: Int,
    val title: String,
    val feedbackText: SpannedString,
    val subtitle: SpannedString,

    val isShareVisible: Boolean,
    val isViewCertificateVisible: Boolean,

    @StringRes
    val primaryActionStringRes: Int,
    @StringRes
    val secondaryActionStringRes: Int
) {
    companion object {
        val EMPTY = CourseCompleteDialogViewInfo(-1, -1, "", SpannedString(""), SpannedString(""), false, false, -1, -1)
    }
}