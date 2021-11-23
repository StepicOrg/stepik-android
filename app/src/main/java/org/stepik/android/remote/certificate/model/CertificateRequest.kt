package org.stepik.android.remote.certificate.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Certificate

class CertificateRequest(
    @SerializedName("certificate")
    val certificate: Certificate
)