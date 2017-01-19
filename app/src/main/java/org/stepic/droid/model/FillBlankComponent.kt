package org.stepic.droid.model

class FillBlankComponent {
    var text: String? = null
    var type: Type? = null
    var options: List<String>? = null

    enum class Type {
        text,
        input,
        select
    }
}
