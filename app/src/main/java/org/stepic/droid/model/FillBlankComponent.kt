package org.stepic.droid.model

import com.google.gson.annotations.Expose

class FillBlankComponent {
    var text: String? = null
    var type: Type? = null
    var options: List<String>? = null

    @Expose
    var defaultValue: String? = null


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
