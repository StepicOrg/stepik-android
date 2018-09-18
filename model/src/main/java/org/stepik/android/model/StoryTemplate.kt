package org.stepik.android.model

import com.google.gson.annotations.SerializedName

data class StoryTemplate(
        val id: Long,
        val cover: String,
        val title: String,

        @SerializedName("is_published")
        val isPublished: Boolean,

        val parts: List<Part>,

        val language: String,
        val position: Int,
        val version: Int
) {
    data class Part(
            val duration: Long,
            val image: String,
            val position: Int,
            val type: String,

            val button: Button?,
            val text: Text?
    )

    data class Text(
            @SerializedName("background_style")
            val backgroundStyle: String,

            @SerializedName("text")
            val text: String,

            @SerializedName("text_color")
            val textColor: String,

            @SerializedName("title")
            val title: String
    )

    data class Button(
            @SerializedName("background_color")
            val backgroundColor: String,

            @SerializedName("text_color")
            val textColor: String,

            @SerializedName("title")
            val title: String,

            @SerializedName("url")
            val url: String
    )
}