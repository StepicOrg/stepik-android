package org.stepik.android.model

import ru.nobird.android.core.model.Identifiable

enum class PlatformType(
    override val id: Int,
    val title: String
) : Identifiable<Int> {
    WEB(1, "web"),
    MOBILE(2, "mobile"),
    SUNION_PLUGIN(3, "sunion_plugin"),
    ANDROID(4, "android"),
    IOS(5, "ios")
}