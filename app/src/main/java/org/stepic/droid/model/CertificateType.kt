package org.stepic.droid.model

import com.google.gson.annotations.SerializedName

enum class CertificateType() {
    @SerializedName("regular")
    regular,
    @SerializedName("distinction")
    distinction
}