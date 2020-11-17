package org.stepik.android.domain.course_collection.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.PlatformType
import ru.nobird.android.core.model.mapOfNotNull

data class CourseCollectionQuery(
    @SerializedName("language")
    val language: String? = null,

    @SerializedName("platform")
    val platform: List<PlatformType> = listOf(PlatformType.MOBILE, PlatformType.ANDROID)
) {
    companion object {
        private const val LANGUAGE = "language"
        private const val PLATFORM = "platform"
    }

    fun toMap(): Map<String, String> =
        mapOfNotNull(
            LANGUAGE to language,
            PLATFORM to platform.joinToString(separator = ",", transform = PlatformType::title)
        )
}