package org.stepik.android.model.attempts

import com.google.gson.annotations.SerializedName

class Dataset(
        val options: List<String>? = null,
        val someStringValueFromServer: String? = null,
        val pairs: List<Pair>? = null,
        val components: List<FillBlankComponent>? = null,
        val rows: List<String>? = null,
        val columns: List<String>? = null,
        val description: String? = null,

        @SerializedName("is_multiple_choice")
        val isMultipleChoice: Boolean = false,
        @SerializedName("is_checkbox")
        val isCheckbox: Boolean = false,
        @SerializedName("is_html_enabled")
        val isHtmlEnabled: Boolean = false
)

class DatasetWrapper(val dataset: Dataset? = null)