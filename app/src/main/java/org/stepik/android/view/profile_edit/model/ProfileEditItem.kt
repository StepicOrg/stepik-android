package org.stepik.android.view.profile_edit.model

import ru.nobird.android.core.model.Identifiable

data class ProfileEditItem(
    val type: Type,
    val title: String,
    val subtitle: String
) : Identifiable<ProfileEditItem.Type> {
    override val id: Type = type

    enum class Type {
        PERSONAL_INFO,
        PASSWORD
    }
}