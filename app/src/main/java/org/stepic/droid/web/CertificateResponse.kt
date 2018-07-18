package org.stepic.droid.web

import org.stepik.android.model.structure.Certificate
import org.stepik.android.model.Meta

data class CertificateResponse(
        val meta: Meta?,
        val certificates: List<Certificate>
)