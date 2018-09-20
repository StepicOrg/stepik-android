package org.stepic.droid.web

import org.stepik.android.model.Certificate
import org.stepik.android.model.Meta

class CertificateResponse(
        val meta: Meta?,
        val certificates: List<Certificate>
)