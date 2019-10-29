package org.stepik.android.view.profile_edit.model

data class ProfileEditItem(
    val type: Type,
    val title: String,
    val subtitle: String
) {
    enum class Type {
        PERSONAL_INFO,
        EMAIL,
        PASSWORD
    }
}