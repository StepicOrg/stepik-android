package org.stepik.android.remote.certificates.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Certificate
import org.stepik.android.model.Meta

class CertificateResponse(
    @SerializedName("meta")
    val meta: Meta?,
    @SerializedName("certificates")
    val certificates: List<Certificate>
)