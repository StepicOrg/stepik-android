package org.stepik.android.view.step_quiz_code.model

sealed class CodeDetail {
    data class Sample(val position: Int, val input: String, val output: String) : CodeDetail()

    data class Limit(val value: Int, val type: Type) : CodeDetail() {
        enum class Type {
            MEMORY, TIME
        }
    }
}