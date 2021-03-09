package org.stepik.android.cache.code_preference.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CodePreference(
    @PrimaryKey
    val languagesKey: Set<String>,
    val preferredLanguage: String
) {
    companion object {
        val EMPTY = CodePreference(emptySet(), "")
    }
}