package org.stepic.droid.presenters.certificate

import org.stepic.droid.core.CertificateView

class CertificatePresenterImpl : CertificatePresenter {

    var certificateView: CertificateView? = null

    override fun onCreate(certificateView: CertificateView) {
        this.certificateView = certificateView
    }

    override fun onDestroy() {
        certificateView = null
    }
}
