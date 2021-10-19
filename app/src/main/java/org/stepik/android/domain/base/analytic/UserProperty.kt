package org.stepik.android.domain.base.analytic

import java.util.EnumSet

interface UserProperty {
    val name: String
    val value: Any

    val sources: EnumSet<UserPropertySource>
        get() = EnumSet.allOf(UserPropertySource::class.java)
}