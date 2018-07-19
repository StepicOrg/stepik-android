package org.stepik.android.model.attempts

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
                    text -> false
                    input -> true
                    select -> true
                }
    }
}
