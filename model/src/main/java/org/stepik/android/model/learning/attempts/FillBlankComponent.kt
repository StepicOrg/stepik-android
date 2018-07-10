package org.stepik.android.model.learning.attempts

class FillBlankComponent(
        val text: String? = null,
        val type: Type? = null,
        val options: List<String>? = null,
        var defaultValue: String? = null
) {
    enum class Type {
        text,
        input,
        select;

        fun canSubmit() =
                when (this) {
                    FillBlankComponent.Type.text -> false
                    FillBlankComponent.Type.input -> true
                    FillBlankComponent.Type.select -> true
                }
    }
}
