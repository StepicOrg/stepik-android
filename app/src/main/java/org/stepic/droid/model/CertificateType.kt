package org.stepic.droid.model

import com.google.gson.annotations.SerializedName

/*
Add new in the end, because serialization depends on order.
 */
enum class CertificateType() {
    @SerializedName("regular")
    regular,
    @SerializedName("distinction")
    distinction
}