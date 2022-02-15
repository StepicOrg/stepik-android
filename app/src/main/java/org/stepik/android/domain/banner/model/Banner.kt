package org.stepik.android.domain.banner.model

import com.google.gson.annotations.SerializedName

data class Banner(
    @SerializedName("title")
    val title: String,
    @SerializedName("type")
    val type: ColorType,
    @SerializedName("lang")
    val language: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("screen")
    val screen: Screen,
    @SerializedName("position")
    val position: Int
) {
    enum class ColorType(val type: String) {
        @SerializedName("blue")
        BLUE("blue"),

        @SerializedName("violet")
        VIOLET("violet"),

        @SerializedName("green")
        GREEN("green")
    }

    enum class Screen(val screen: String) {
        @SerializedName("catalog")
        CATALOG("catalog"),

        @SerializedName("home")
        HOME("home")
    }
}
