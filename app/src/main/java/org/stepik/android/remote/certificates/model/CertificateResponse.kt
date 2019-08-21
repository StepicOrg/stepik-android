package org.stepik.android.remote.certificates.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Certificate
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class CertificateResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("certificates")
    val certificates: List<Certificate>
) : MetaResponse