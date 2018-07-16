package org.stepik.android.model.learning.certificates

import com.google.gson.annotations.SerializedName

/*
Add new in the end, because serialization depends on order.
 */
enum class CertificateType {
    @SerializedName("regular")
    REGULAR,
    @SerializedName("distinction")
    DISTINCTION
}