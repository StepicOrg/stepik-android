package org.stepic.droid.web

import org.stepic.droid.model.Certificate
import org.stepic.droid.model.Meta

data class CertificateResponse(
        val meta: Meta?,
        val certificates: List<Certificate>)